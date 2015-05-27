/*
 * Copyright 2013 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 * 
 * Based on libusbx <http://libusbx.org/>:  
 * 
 * Copyright 2001 Johannes Erdfelt <johannes@erdfelt.com>
 * Copyright 2007-2008 Daniel Drake <dsd@gentoo.org>
 * Copyright 2012 Pete Batard <pete@akeo.ie>
 */

package de.ailis.usb4java.libusb;

import java.nio.ByteBuffer;

import javax.usb.UsbConfigurationDescriptor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.ailis.usb4java.utils.DescriptorUtils;

/**
 * A structure representing the standard USB configuration descriptor.
 * 
 * This descriptor is documented in section 9.6.3 of the USB 3.0 specification.
 * All multiple-byte fields are represented in host-endian format.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public final class ConfigDescriptor implements UsbConfigurationDescriptor
{
    /** The native pointer to the descriptor structure. */
    private long configDescriptorPointer;

    /**
     * Constructs a new config descriptor which can be passed to the
     * {@link LibUsb#getConfigDescriptor(Device, int, ConfigDescriptor)} method.
     */
    public ConfigDescriptor()
    {
        // Empty
    }

    /**
     * Returns the native pointer.
     * 
     * @return The native pointer.
     */
    public long getPointer()
    {
        return this.configDescriptorPointer;
    }

    @Override
    public native byte bLength();

    @Override
    public native byte bDescriptorType();

    @Override
    public native short wTotalLength();

    @Override
    public native byte bNumInterfaces();

    @Override
    public native byte bConfigurationValue();

    @Override
    public native byte iConfiguration();

    @Override
    public native byte bmAttributes();

    @Override
    public native byte bMaxPower();

    /**
     * Returns the array with interfaces supported by this configuration.
     * 
     * @return The array with interfaces.
     */
    public native Interface[] iface();

    /**
     * Extra descriptors.
     * 
     * If libusbx encounters unknown interface descriptors, it will store them
     * here, should you wish to parse them.
     * 
     * @return The extra descriptors.
     */
    public native ByteBuffer extra();

    /**
     * Length of the extra descriptors, in bytes.
     * 
     * @return The extra descriptors length.
     */
    public native int extraLength();

    /**
     * Returns a dump of this descriptor.
     * 
     * @return The descriptor dump.
     */
    public String dump()
    {
        return dump(null);
    }

    /**
     * Returns a dump of this descriptor.
     * 
     * @param handle
     *            The USB device handle for resolving string descriptors. If
     *            null then no strings are resolved.
     * @return The descriptor dump.
     */
    public String dump(final DeviceHandle handle)
    {
        final StringBuilder builder = new StringBuilder();
        builder
            .append(String.format("%s%n"
                + "  extralen %17d%n"
                + "  extra:%n"
                + "%s",
                DescriptorUtils.dump(this),
                extraLength(),
                DescriptorUtils.dump(extra()).replaceAll("(?m)^", "    ")));
        for (final Interface descriptor: iface())
        {
            builder.append(descriptor.dump(handle)
                .replaceAll("(?m)^", "  "));
        }
        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        final ConfigDescriptor other = (ConfigDescriptor) obj;
        return new EqualsBuilder()
            .append(bDescriptorType(), other.bDescriptorType())
            .append(bLength(), other.bLength())
            .append(bConfigurationValue(), other.bConfigurationValue())
            .append(bmAttributes(), other.bmAttributes())
            .append(bNumInterfaces(), other.bNumInterfaces())
            .append(iConfiguration(), other.iConfiguration())
            .append(bMaxPower(), other.bMaxPower())
            .append(wTotalLength(), other.wTotalLength()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
            .append(bLength())
            .append(bDescriptorType())
            .append(wTotalLength())
            .append(bNumInterfaces())
            .append(bConfigurationValue())
            .append(iConfiguration())
            .append(bmAttributes())
            .append(bMaxPower())
            .append(extra())
            .append(extraLength())
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return dump();
    }
}
