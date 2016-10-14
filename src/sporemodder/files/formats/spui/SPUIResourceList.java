package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class SPUIResourceList implements Iterable<SPUIResource> {
	
	private final List<SPUIFileResource> fileResources = new ArrayList<SPUIFileResource>();
	private final List<SPUIFileResource> atlasResources = new ArrayList<SPUIFileResource>();
	private final List<SPUIResourceType3> type3Resources = new ArrayList<SPUIResourceType3>();
	private final List<SPUIStructResource> structResources = new ArrayList<SPUIStructResource>();
	
	@Override
    public Iterator<SPUIResource> iterator() {
        Iterator<SPUIResource> it = new Iterator<SPUIResource>() {

        	private int currentIndex = 0;
        	private int resourceCount = getResourceCount();
        	
			@Override
			public boolean hasNext() {
				return currentIndex < resourceCount;
			}

			@Override
			public SPUIResource next() {
				return get(currentIndex++);
			}
        	
        };
        return it;
    }
	
	public SPUIResource get(int index) {
		int size = fileResources.size();
		if (index < size) {
			return fileResources.get(index);
		}
		index -= size;
		size = atlasResources.size();
		if (index < size) {
			return atlasResources.get(index);
		}
		index -= size;
		size = type3Resources.size();
		if (index < size) {
			return type3Resources.get(index);
		}
		index -= size;
		size = structResources.size();
		if (index < size) {
			return structResources.get(index);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SPUIResource> T get(int index, Class<T> clazz) {
		SPUIResource res = get(index);
		if (res == null) {
			return null;
		}
		if (!clazz.isInstance(res)) {
			throw new IllegalArgumentException("The resource is not of the specified type.");
		}
		return (T) res;
	}
	
	public void add(SPUIResource resource) {
		if (resource instanceof SPUIFileResource) {
			SPUIFileResource res = (SPUIFileResource) resource;
			if (res.isAtlas) {
				atlasResources.add(res);
			} else {
				fileResources.add(res);
			}
		}
		else if (resource instanceof SPUIResourceType3) {
			type3Resources.add((SPUIResourceType3) resource);
		}
		else if (resource instanceof SPUIStructResource) {
			structResources.add((SPUIStructResource) resource);
		}
		else {
			throw new UnsupportedOperationException("Unsupported SPUI resource type.");
		}
	
	}
	
	public void add(SPUIFileResource resource) {
		if (resource.isAtlas) {
			atlasResources.add(resource);
		} else {
			fileResources.add(resource);
		}
	}
	public void add(SPUIResourceType3 resource) {
		type3Resources.add(resource);
	}
	public void add(SPUIStructResource resource) {
		structResources.add(resource);
	}

	public int getResourceCount() {
		return fileResources.size() + atlasResources.size() + type3Resources.size() + structResources.size();
	}
	
	public void clear() {
		fileResources.clear();
		atlasResources.clear();
		type3Resources.clear();
		structResources.clear();
	}
	
	
	public void read(InputStreamAccessor in, int version) throws IOException {
		int resCount1 = in.readLEShort();
		int resCount2 = in.readLEShort();
		int resCount3 = in.readLEShort();
		int resCount4 = in.readLEShort();
		int resourceCount = resCount1 + resCount2 + resCount3 + resCount4;
		// Spore doesn't support more than 6000 resources
		if (resourceCount > 6000) {
			throw new IOException("Too many resourcse in SPUIResourceList. Max 6000 supported.");
		}
		
		for (int i = 0; i < resCount1; i++) {
			SPUIFileResource resource = new SPUIFileResource();
			resource.read(in, version);
			fileResources.add(resource);
		}
		
		for (int i = 0; i < resCount2; i++) {
			SPUIFileResource resource = new SPUIFileResource();
			resource.read(in, version);
			resource.isAtlas = true;
			atlasResources.add(resource);
		}
		
		for (int i = 0; i < resCount3; i++) {
			SPUIResourceType3 resource = new SPUIResourceType3();
			resource.read(in, version);
			type3Resources.add(resource);
		}
		
		for (int i = 0; i < resCount4; i++) {
			SPUIStructResource resource = new SPUIStructResource();
			resource.read(in, version);
			structResources.add(resource);
		}
	}
	
	public void write(OutputStreamAccessor out, int version) throws IOException {
		out.writeLEShort(fileResources.size());
		out.writeLEShort(atlasResources.size());
		out.writeLEShort(type3Resources.size());
		out.writeLEShort(structResources.size());
		for (SPUIResource res : this) {
			res.write(out, version);
		}
	}
	
	public int indexOf(SPUIResource resource) {
		int indexOf = fileResources.indexOf(resource);
		if (indexOf != -1) {
			return indexOf;
		}
		int size = fileResources.size();
		indexOf = atlasResources.indexOf(resource);
		if (indexOf != -1) {
			return indexOf + size;
		}
		size += atlasResources.size();
		indexOf = type3Resources.indexOf(resource);
		if (indexOf != -1) {
			return indexOf + size;
		}
		size += type3Resources.size();
		indexOf = structResources.indexOf(resource);
		if (indexOf != -1) {
			return indexOf + size;
		}
		return -1;
	}

	public List<SPUIFileResource> getFileResources() {
		return fileResources;
	}

	public List<SPUIFileResource> getAtlasResources() {
		return atlasResources;
	}

	public List<SPUIResourceType3> getType3Resources() {
		return type3Resources;
	}

	public List<SPUIStructResource> getStructResources() {
		return structResources;
	}
	
	// Returns the number of resources that can be used in 'short' sections
	public int getValidResourcesCount() {
		return fileResources.size() + atlasResources.size() + type3Resources.size();
	}
	
	
	public int getNextFileIndex() {
		return fileResources.size();
	}
	
	public int getNextAtlasIndex() {
		return fileResources.size() + atlasResources.size();
	}
	
	public int getNextType3Index() {
		return fileResources.size() + atlasResources.size() + type3Resources.size();
	}
	
	public int getNextStructIndex() {
		return fileResources.size() + atlasResources.size() + type3Resources.size() + structResources.size();
	}
}
