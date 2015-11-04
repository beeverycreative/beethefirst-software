using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Linq;
using System.Text;
using System.Windows.Forms; // MessageBox
using System.IO;

namespace sample3dscan.cs
{
    class RenderStreams
    {

        private MainForm form;

        public RenderStreams(MainForm mf)
        {
            form = mf;
        }

        public static byte[] GetRGB32Pixels(PXCMImage image, out int cwidth, out int cheight)
        {
            PXCMImage.ImageData cdata;
            byte[] cpixels = null;
            cwidth = cheight = 0;
            if (image.AcquireAccess(PXCMImage.Access.ACCESS_READ, PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32, out cdata) >= pxcmStatus.PXCM_STATUS_NO_ERROR)
            {
                cwidth = (int)cdata.pitches[0] / sizeof(Int32);
                cheight = (int)image.info.height;
                cpixels = cdata.ToByteArray(0, (int)cdata.pitches[0] * cheight);
                image.ReleaseAccess(cdata);
            }
            return cpixels;
        }

        void OnAlert(PXCM3DScan.AlertData data)
        {
            try
            {
                switch (data.label)
                {
                    case PXCM3DScan.AlertEvent.ALERT_TOO_FAR:
                        {
                            form.Invoke(new Action(() => form.ShowTooFar()));
                            break;
                        }
                    case PXCM3DScan.AlertEvent.ALERT_TOO_CLOSE:
                        {
                            form.Invoke(new Action(() => form.ShowTooClose()));
                            break;
                        }
                    case PXCM3DScan.AlertEvent.ALERT_IN_RANGE:
                        {
                            form.Invoke(new Action(() => form.HideRangeAlerts()));
                            break;
                        }
                    case PXCM3DScan.AlertEvent.ALERT_LOST_TRACKING:
                        {
                            form.Invoke(new Action(() => form.ShowTrackingAlert()));
                            break;
                        }
                    case PXCM3DScan.AlertEvent.ALERT_TRACKING:
                        {
                            form.Invoke(new Action(() => form.HideTrackingAlert()));
                            break;
                        }
                }
            }
            catch { };
        }

        public void StreamColorDepth(String scanType) /* Stream Color and Depth Synchronously or Asynchronously */
        {
            bool sts = true;
            PXCM3DScan.Configuration scan_config = new PXCM3DScan.Configuration();
            String statusString;

            /* Create an instance of the PXCSenseManager interface */
            PXCMSenseManager pp = PXCMSenseManager.CreateInstance();
            if (pp == null)
            {
                form.UpdateStatus("Failed to create an SDK pipeline object");
                return;
            }
            if (!form.IsModeLive())
            {
                pp.captureManager.SetFileName(form.GetFileName(), form.IsModeRecord());
                if (!form.IsModeRecord())
                {
                    // Disable realtime mode if we are playing back a file.
                    pp.captureManager.SetRealtime(false);
                }
            }

            /* Set Input Source */
            PXCMCapture.DeviceInfo dinfo2 = form.GetCheckedDevice();
            if (form.IsModeLive() || form.IsModeRecord()) pp.captureManager.FilterByDeviceInfo(dinfo2);

            /* Delay recording frames until the scan starts */
            if (form.IsModeRecord()) pp.captureManager.SetPause(true);

            /* Set Color & Depth Resolution */
            PXCMCapture.Device.StreamProfile cinfo = form.GetColorConfiguration();
            if (cinfo.imageInfo.format != 0)
            {
                Single cfps = cinfo.frameRate.max;
                pp.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, cinfo.imageInfo.width, cinfo.imageInfo.height, cfps);
            }

            PXCMCapture.Device.StreamProfile dinfo = form.GetDepthConfiguration();
            if (dinfo.imageInfo.format != 0)
            {
                Single dfps = dinfo.frameRate.max;
                pp.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_DEPTH, dinfo.imageInfo.width, dinfo.imageInfo.height, dfps);
            }

            /* Initialization */
            FPSTimer timer = new FPSTimer(form);
            if (form.IsModeLive())
            {
                form.UpdateStatus("Center object in the scanning area, then press Start Scanning");
            }
            /* Enable the 3D Scan video module */
            pxcmStatus result = pp.Enable3DScan();
            if (result != pxcmStatus.PXCM_STATUS_NO_ERROR)
            {
                pp.Close();
                pp.Dispose();
                form.UpdateStatus("Enable3DScan() returned " + result);
                return;
            }

            /* Initialize the camera system */
            result = pp.Init();
            if (result >= pxcmStatus.PXCM_STATUS_NO_ERROR)
            {
                /* Setup the scanning configuration */
                if (scanType == "Object") scan_config.mode = PXCM3DScan.ScanningMode.OBJECT_ON_PLANAR_SURFACE_DETECTION;
                else if (scanType == "Face") scan_config.mode = PXCM3DScan.ScanningMode.FACE;
                else if (scanType == "Body") scan_config.mode = PXCM3DScan.ScanningMode.BODY;
                else if (scanType == "Head") scan_config.mode = PXCM3DScan.ScanningMode.HEAD;
                else if (scanType == "Full") scan_config.mode = PXCM3DScan.ScanningMode.VARIABLE;
                
                /* Select the Targeting Options */
                scan_config.options = PXCM3DScan.ReconstructionOption.NONE;
                if (form.isSolidificationSelected()) scan_config.options |= (PXCM3DScan.ReconstructionOption.SOLIDIFICATION);
                if (form.isTextureSelected()) scan_config.options |= (PXCM3DScan.ReconstructionOption.TEXTURE);

                /* Initialize the scanning system */
                PXCM3DScan scan = pp.Query3DScan();
                if (scan == null || scan.SetConfiguration(scan_config) < pxcmStatus.PXCM_STATUS_NO_ERROR)
                {
                    if (scan == null) form.UpdateStatus("3DScan module not found.");
                    else
                    {
                        scan.Dispose();
                        form.UpdateStatus("SetConfiguration returned an error.");
                    }
                    sts = false;
                }
                else
                {
                    /* Optionally subscribe to recieve range and tracking alerts 
                     (e.g. too close, lost tracking) */
                    scan.Subscribe(OnAlert);

                    Projection projection = new Projection(
                        pp.session, pp.captureManager.device, dinfo.imageInfo);

                    Boolean bStreaming = false;
                    Boolean bScanning = false;
                    while (form.reconstruct_requested || !form.GetStopState())
                    {
                        if (form.scan_requested) /* one time latch */
                        {
                            form.scan_requested = false;

                            /* Disable ColorAuto* to improve the color consistency */
                            pp.captureManager.device.SetColorAutoExposure(false);
                            pp.captureManager.device.SetColorAutoWhiteBalance(false);

                            /* Update the configuration to force the scan to start as soon as possible */
                            scan_config.startScan = true;
                            scan.SetConfiguration(scan_config);

                            /* Update the status bar to help users understand what the detector is looking for */
                            if (scan_config.mode == PXCM3DScan.ScanningMode.OBJECT_ON_PLANAR_SURFACE_DETECTION
                                && form.IsModeLive())
                            {
                                form.UpdateStatus("Place the object on a flat surface. Once in view, press Start Scanning...");
                            }
                        }
                        else if (form.reconstruct_requested)
                        {
                            sts = SaveMesh(scan);

                            //Exits the application after saving the file
                            Application.Exit();
                        }

                        /* Get preview image from the 3D Scan video module */
                        if (!form.GetStopState())
                        {
                            /* Wait until a frame is ready: Synchronized or Asynchronous */
                            if (pp.AcquireFrame() < pxcmStatus.PXCM_STATUS_NO_ERROR)
                            {
                                projection.Dispose();
                                if (!form.IsModeLive())
                                {
                                    form.Invoke(new Action(() => form.EndScan()));
                                    sts = SaveMesh(scan);
                                }
                                break;
                            }

                            // Enable the Start Scanning button only after the camera starts streaming
                            if (!bStreaming)
                            {
                                bStreaming = true;
                                form.Invoke(new Action(() => form.EnableScanReconstruct()));
                            }

                            /* Get preview image from the 3D Scan video module */
                            PXCMImage preview_image = scan.AcquirePreviewImage();
                            pp.ReleaseFrame();
                                                   
                            /* Display Image and Status */
                            if (preview_image != null)
                            {
                                form.SetBitmap(preview_image);

                                if (scan.IsScanning())
                                {
                                    statusString = "Scanning";
                                    timer.Tick(statusString + " ");
                                    if (bScanning == false)
                                    {
                                        bScanning = true;
                                        form.Invoke(new Action(() => form.EnableScanReconstruct()));
                                        /* Delay recording frames until the start of the scan */
                                        if (form.IsModeRecord()) pp.captureManager.SetPause(false);
                                    }
                                }
                                preview_image.Dispose();
                            }
                        }
                    }
                    projection.Dispose();
                    scan.Dispose();
                }
                /* Reenable ColorAuto* */
                pp.captureManager.device.SetColorAutoExposure(true);
                pp.captureManager.device.SetColorAutoWhiteBalance(true);
            }
            else
            {
                form.UpdateStatus("Init() returned " + result);
                sts = false;
            }
            if (sts) form.UpdateStatus("");
            pp.Close();
            pp.Dispose();
            
            try
            {
                form.Invoke(new Action(() => form.ResetStop()));
            }
            catch { }

        }

        private struct vertexStruct
        {
            public float x, y, z;
            public float r, g, b;
        };
        private struct faceStruct
        {
            public Int32[] vertex_indicies;
        };
        private struct meshStruct
        {
            public vertexStruct[] vertices;
            public faceStruct[] faces;
        };

        private meshStruct LoadObjFile(string in_obj_file_name)
        {
            meshStruct mesh = new meshStruct();
            StreamReader stream = File.OpenText(in_obj_file_name);
            string text = stream.ReadToEnd();
            stream.Close();
            const int CHUNK_SIZE = 50000;
            int maxVerts = CHUNK_SIZE;
            mesh.vertices = new vertexStruct[maxVerts];
            int maxFaces = CHUNK_SIZE;
            mesh.faces = new faceStruct[maxFaces];
            int vertices = 0;
            int faces = 0;
            using (StringReader reader = new StringReader(text))
            {
                string currentText = reader.ReadLine();
                char[] split = { ' ' };
                string[] elements;
                while (currentText != null)
                {
                    if (!currentText.StartsWith("f ") && !currentText.StartsWith("v "))
                    {
                        currentText = reader.ReadLine();
                        if (currentText != null) currentText = currentText.Replace("  ", " ");
                    }
                    else
                    {
                        currentText = currentText.Trim();
                        elements = currentText.Split(split, 50);
                        switch (elements[0])
                        {
                            case "v":
                                mesh.vertices[vertices].x = System.Convert.ToSingle(elements[1]);
                                mesh.vertices[vertices].y = System.Convert.ToSingle(elements[2]);
                                mesh.vertices[vertices].z = System.Convert.ToSingle(elements[3]);
                                mesh.vertices[vertices].r = System.Convert.ToSingle(elements[4]);
                                mesh.vertices[vertices].g = System.Convert.ToSingle(elements[5]);
                                mesh.vertices[vertices].b = System.Convert.ToSingle(elements[6]);
                                if (++vertices == maxVerts)
                                {
                                    maxVerts += CHUNK_SIZE;
                                    Array.Resize(ref mesh.vertices, maxVerts);
                                }
                                break;
                            case "f":
                                char[] index_split = { '/' };
                                string[] index_elements;
                                mesh.faces[faces].vertex_indicies = new Int32[3];
                                index_elements = elements[1].Split(index_split, 50);
                                mesh.faces[faces].vertex_indicies[0] = System.Convert.ToInt32(index_elements[0]);
                                index_elements = elements[2].Split(index_split, 50);
                                mesh.faces[faces].vertex_indicies[1] = System.Convert.ToInt32(index_elements[0]);
                                index_elements = elements[3].Split(index_split, 50);
                                mesh.faces[faces].vertex_indicies[2] = System.Convert.ToInt32(index_elements[0]);
                                if (++faces == maxFaces)
                                {
                                    maxFaces += CHUNK_SIZE;
                                    Array.Resize(ref mesh.faces, maxFaces);
                                }
                                break;
                        }
                        currentText = reader.ReadLine();
                        if (currentText != null) currentText = currentText.Replace("  ", " ");
                    }
                }
            }
            Array.Resize(ref mesh.vertices, vertices);
            Array.Resize(ref mesh.faces, faces);
            return mesh;
        }

        private StreamReader GetPrefabStream(string filename)
        {
            StreamReader stream = null;
            String path = null;
            // Start by looking in the dev (relative) path
            try
            {
                path = @"..\..\data\3dscan\viewer\" + filename;
                stream = File.OpenText(path);
            }
            catch { };
            // Then try the installed location
            if (stream == null)
            {
                try
                {
                    string RSSDK_DIR = Environment.GetEnvironmentVariable("RSSDK_DIR");
                    if (RSSDK_DIR != null) path = RSSDK_DIR + @"data\3dscan\viewer\" + filename;
                    stream = File.OpenText(path);
                }
                catch { };
            }
            // Lastly, look in the local directory
            if (stream == null) try { stream = File.OpenText(filename); }
                catch { };
            return stream;
        }

        public bool GenerateHtmlFromObj(string in_obj_file_name, string out_html_file_name)
        {
            // Load the mesh
            meshStruct mesh = LoadObjFile(in_obj_file_name);
            if (mesh.vertices.Length == 0 || mesh.faces.Length == 0) return true;

            // Write out the header
            StreamReader stream = GetPrefabStream(@"3dscan_header.html");
            if (stream == null) return true;
            string text = stream.ReadToEnd();
            stream.Close();
            File.WriteAllText(out_html_file_name, text);

            // Then, compose and append the data vertex...
            {
                StringBuilder sbv = new StringBuilder();
                StringBuilder sbc = new StringBuilder();
                string PRECISION = "0.000000";
                sbv.Append("    var verticies = new Float64Array([");
                sbc.Append("    var colors = new Uint8Array([");
                for (Int32 v = 0; v < mesh.vertices.Length; v++)
                {
                    vertexStruct vert = mesh.vertices[v];
                    sbv.Append(vert.x.ToString(PRECISION) + ","
                             + vert.y.ToString(PRECISION) + ","
                             + vert.z.ToString(PRECISION) + ",");
                    sbc.Append((vert.r * 255).ToString("0.") + ","
                             + (vert.g * 255).ToString("0.") + ","
                             + (vert.b * 255).ToString("0.") + ",");
                }
                sbv.Append("]);\n");
                File.AppendAllText(out_html_file_name, sbv.ToString());
                sbc.Append("]);\n");
                File.AppendAllText(out_html_file_name, sbc.ToString());
            }
            // ...and face data.
            {
                StringBuilder sbf = new StringBuilder();
                sbf.Append("    var faces = new Int32Array([");
                for (Int32 f = 0; f < mesh.faces.Length; f++)
                {
                    faceStruct face = mesh.faces[f];
                    sbf.Append((face.vertex_indicies[0] - 1).ToString() + ","
                             + (face.vertex_indicies[1] - 1).ToString() + ","
                             + (face.vertex_indicies[2] - 1).ToString() + ",");

                }
                sbf.Append("]);\n");
                File.AppendAllText(out_html_file_name, sbf.ToString());
            }

            // Lastly, append the footer
            stream = GetPrefabStream(@"3dscan_footer.html");
            if (stream == null) return true;
            text = stream.ReadToEnd();
            stream.Close();
            File.AppendAllText(out_html_file_name, text);

            return false;
        }

        private bool SaveMesh(PXCM3DScan scan)
        {
            form.Invoke(new Action(() => form.HideTrackingAlert()));
            form.Invoke(new Action(() => form.HideRangeAlerts()));

            bool sts = true;

            form.UpdateStatus("");

            // wait for filename to be filled
            string filename = null;
            do
            {
                filename = form.GetMeshFileName();
                System.Threading.Thread.Sleep(5);
            } while (filename == null);

            if (filename != "c")
            {
                form.UpdateStatus("Saving " + filename + "...");

                string mesh_format = filename.Substring(filename.Length - 3);
                pxcmStatus res = pxcmStatus.PXCM_STATUS_NO_ERROR;

                if (mesh_format.ToLower() == "obj") res = scan.Reconstruct(PXCM3DScan.FileFormat.OBJ, filename);
                else if (mesh_format.ToLower() == "ply") res = scan.Reconstruct(PXCM3DScan.FileFormat.PLY, filename);
                else if (mesh_format.ToLower() == "stl") res = scan.Reconstruct(PXCM3DScan.FileFormat.STL, filename);

                if (res < pxcmStatus.PXCM_STATUS_NO_ERROR)
                {
                    if (res == pxcmStatus.PXCM_STATUS_FILE_WRITE_FAILED) form.UpdateStatus("Error: Write failed to " + filename + " Exiting...");
                    else form.UpdateStatus("Reconstruct() returned " + res);
                    sts = false;
                }
                else
                {
                    form.UpdateStatus(filename + " saved");
                    sts = false;

                    if ((mesh_format.ToLower() == "obj") && !form.isTextureSelected())
                    {
                        // Generate an html version of the obj file
                        String htmlFile = filename + ".html";
                        if (!GenerateHtmlFromObj(filename, htmlFile))
                        {
                            // Load the resulting html file
                            GC.Collect(); // Free up as much system RAM as possible before loading the html file.
                            System.Diagnostics.Process process = new System.Diagnostics.Process();
                            process.StartInfo.WindowStyle = System.Diagnostics.ProcessWindowStyle.Hidden;
                            process.StartInfo.FileName = (string)htmlFile;
                            process.Start();
                            process.Dispose();
                        }
                    }
                }
            }
            else
            {
                form.UpdateStatus("Canceled");
            }
            form.reconstruct_requested = false;
            form.ReleaseMeshFileName();

            return sts;
        }
    }
}
