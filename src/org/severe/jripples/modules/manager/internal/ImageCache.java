/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.modules.manager.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * @author Maksym Petrenko
 * 
 */
public class ImageCache {


	private final Map imageMap = new HashMap();

	public Image getImage(ImageDescriptor imageDescriptor) {
		if (imageDescriptor == null)
			return null;
		Image image = (Image) imageMap.get(imageDescriptor);
		if (image == null) {
			image = imageDescriptor.createImage();
			imageMap.put(imageDescriptor, image);
		}
		return image;
	}

	public void dispose() {
		Iterator iter = imageMap.values().iterator();
		while (iter.hasNext())
			((Image) iter.next()).dispose();
		imageMap.clear();

	}
}
