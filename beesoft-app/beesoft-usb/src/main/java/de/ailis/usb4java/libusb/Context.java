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

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Structure representing a libusbx session. The concept of individual libusbx
 * sessions allows for your program to use two libraries (or dynamically load
 * two modules) which both independently use libusb. This will prevent
 * interference between the individual libusbx users - for example
 * {@link LibUsb#setDebug(Context, int)} will not affect the other user of the
 * library, and {@link LibUsb#exit(Context)} will not destroy resources that the
 * other user is still using.
 * 
 * Sessions are created by {@link LibUsb#init(Context)} and destroyed through
 * {@link LibUsb#exit(Context)}. If your application is guaranteed to only ever
 * include a single libusbx user (i.e. you), you do not have to worry about
 * contexts: pass NULL in every function call where a context is required. The
 * default context will be used.
 * 
 * For more information, see <a
 * href="http://libusbx.sf.net/api-1.0/contexts.html">Contexts</a>.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public final class Context
{
    /** The native pointer to the context structure. */
    private long contextPointer;
    
    /**
     * Constructs a new libusb context. Must be passed to
     * {@link LibUsb#init(Context)} before passing it to any other method.
     */
    public Context()
    {
        // Empty 
    }

    /**
     * Returns the native pointer to the context structure.
     * 
     * @return The native pointer to the context structure.
     */
    public long getPointer()
    {
        return this.contextPointer;
    }
        
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.contextPointer).toHashCode();
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Context other = (Context) obj;
        return this.contextPointer == other.contextPointer;
    }    
}
