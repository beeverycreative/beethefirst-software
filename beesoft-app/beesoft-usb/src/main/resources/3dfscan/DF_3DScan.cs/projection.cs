using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Linq;
using System.Text;

namespace sample3dscan.cs
{
    class Projection: IDisposable
    {
        private PXCMProjection projection=null;
        private UInt16 invalid_value; /* invalid depth values */
        private PXCMPointF32[] uvmap;

        public Projection(PXCMSession session, PXCMCapture.Device device, PXCMImage.ImageInfo dinfo) {
            /* retrieve the invalid depth pixel values */
            invalid_value = device.QueryDepthLowConfidenceValue();

            /* Create the projection instance */
            projection = device.CreateProjection();

            uvmap = new PXCMPointF32[dinfo.width * dinfo.height]; 
        }

        public void Dispose()
        {
            if (projection==null) return;
            projection.Dispose();
            projection = null;
        }

        private void PlotXY(byte[] cpixels, int xx, int yy, int cwidth, int cheight, int dots, int color) {
            if (xx < 0 || xx >= cwidth || yy < 0 || yy >= cheight) return;

            int lyy = yy * cwidth;
            int xxm1 = (xx > 0 ? xx - 1 : xx), xxp1 = (xx < (int)cwidth - 1 ? xx + 1 : xx);
            int lyym1 = yy > 0 ? lyy - cwidth : lyy, lyyp1 = yy < (int)cheight - 1 ? lyy + cwidth : lyy;

            if (dots >= 9)  /* 9 dots */
            {
                cpixels[(lyym1 + xxm1) * 4 + color] = 0xFF;
                cpixels[(lyym1 + xxp1) * 4 + color] = 0xFF;
                cpixels[(lyyp1 + xxm1) * 4 + color] = 0xFF;
                cpixels[(lyyp1 + xxp1) * 4 + color] = 0xFF;
            }
            if (dots >= 5)  /* 5 dots */
            {
                cpixels[(lyym1 + xx) * 4 + color] = 0xFF;
                cpixels[(lyy + xxm1) * 4 + color] = 0xFF;
                cpixels[(lyy + xxp1) * 4 + color] = 0xFF;
                cpixels[(lyyp1 + xx) * 4 + color] = 0xFF;
            }
            cpixels[(lyy + xx) * 4 + color] = 0xFF; /* 1 dot */
        }

        // GZ: this function needs to be rewrite after projection API is finalized
        //     disable the invokation of this function temporarily
        public byte[] DepthToColorCoordinatesByUVMAP(PXCMImage color, PXCMImage depth, int dots, out int cwidth, out int cheight)
        {
            /* Retrieve the color pixels */
            byte[] cpixels = RenderStreams.GetRGB32Pixels(color, out cwidth, out cheight);
            if (cpixels == null) return cpixels;

            /* Retrieve the depth pixels and uvmap */
            PXCMImage.ImageData ddata;
            Int16[] dpixels;
            // float[] uvmap;
            bool isdepth = (depth.info.format == PXCMImage.PixelFormat.PIXEL_FORMAT_DEPTH);
            if (depth.AcquireAccess(PXCMImage.Access.ACCESS_READ, out ddata) >= pxcmStatus.PXCM_STATUS_NO_ERROR)
            {
                int dwidth = (int)ddata.pitches[0] / sizeof(short); /* aligned width */
                int dheight = (int)depth.info.height;
                dpixels = ddata.ToShortArray(0, isdepth ? dwidth * dheight : dwidth * dheight * 3);
             
                projection.QueryUVMap(depth, uvmap);
                int uvpitch = depth.QueryInfo().width;
                depth.ReleaseAccess(ddata);

                /* Draw dots onto the color pixels */
                for (int y = 0, k = 0; y < dheight; y++)
                {
                    for (int x = 0; x < dwidth; x++, k++)
                    {
                        short d = isdepth ? dpixels[k] : dpixels[3 * k + 2];
                        if (d == invalid_value) continue; // no mapping based on unreliable depth values

                        float uvx = uvmap[k].x, uvy = uvmap[k].y;
                        int xx = (int)(uvx * cwidth + 0.5f), yy = (int)(uvy * cheight + 0.5f);
                        PlotXY(cpixels, xx, yy, cwidth, cheight, dots, 1);
                    }
                }
            }
            return cpixels;
        }

        public byte[] DepthToColorCoordinatesByFunction(PXCMImage color, PXCMImage depth, int dots, out int cwidth, out int cheight)
        {
            /* Retrieve the color pixels */
            byte[] cpixels = RenderStreams.GetRGB32Pixels(color, out cwidth, out cheight);
            if (projection == null || cpixels==null) return cpixels;

            /* Retrieve the depth pixels and uvmap */
            PXCMImage.ImageData ddata;
            UInt16[] dpixels;
            bool isdepth=(depth.info.format == PXCMImage.PixelFormat.PIXEL_FORMAT_DEPTH);
            if (depth.AcquireAccess(PXCMImage.Access.ACCESS_READ, out ddata) >= pxcmStatus.PXCM_STATUS_NO_ERROR)
            {
                int dwidth = ddata.pitches[0]/sizeof(short); /* aligned width */
                int dheight = (int)depth.info.height;
                dpixels = ddata.ToUShortArray(0, isdepth ? dwidth * dheight : dwidth * dheight * 3);
                depth.ReleaseAccess(ddata);

                /* Projection Calculation */
                PXCMPoint3DF32[] dcords = new PXCMPoint3DF32[dwidth * dheight];
                for (int y = 0, k = 0; y < dheight; y++)
                {
                    for (int x = 0; x < dwidth; x++, k++)
                    {
                        dcords[k].x = x;
                        dcords[k].y = y;
                        dcords[k].z = isdepth ? dpixels[k] : dpixels[3 * k + 2];
                    }
                }
                PXCMPointF32[] ccords = new PXCMPointF32[dwidth * dheight];
                projection.MapDepthToColor(dcords, ccords);

                /* Draw dots onto the color pixels */
                for (int y = 0, k = 0; y < dheight; y++)
                {
                    for (int x = 0; x < dwidth; x++, k++)
                    {
                        UInt16 d = isdepth ? dpixels[k] : dpixels[3 * k + 2];
                        if (d == invalid_value) continue; // no mapping based on unreliable depth values

                        int xx = (int)ccords[k].x, yy = (int)ccords[k].y;
                        PlotXY(cpixels, xx, yy, cwidth, cheight, dots, 2);
                    }
                }
            }
            return cpixels;
        }
    }
}
