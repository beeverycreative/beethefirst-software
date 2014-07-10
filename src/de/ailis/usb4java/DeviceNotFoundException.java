/*
 * Copyright (C) 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package de.ailis.usb4java;

/**
 * Thrown when a USB device was not found by id.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public final class DeviceNotFoundException extends RuntimeException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    
    /** The device id. */
    private final DeviceId id;

    /**
     * Constructor.
     *
     * @param id
     *            The ID of the device which was not found.
     */
    DeviceNotFoundException(final DeviceId id)
    {
        super("USB Device not found: " + id);
        this.id = id;
    }

    public DeviceNotFoundException() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the device id.
     *
     * @return The device id.
     */
    public DeviceId getId()
    {
        return this.id;
    }
}
