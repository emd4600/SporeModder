package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4Anim extends RW4Section {
	public static final int type_code = 0x70001;
	public static final int alignment = 16;
	public int[] channel_names;  // duplicate Skeleton.jointInfo, I believe
	public Channel[] channels;
	public int flags; // probably; generally 0-3
	public int skeletonId; // matches Skeleton.jointInfo.id (or something else sometimes!)
	public float length; // seconds?
	public int channelCount; //static
	public int padding;
	public int channelNamesPos, channelInfoPos, channelDataPos, paddingEndPos;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		channelNamesPos = in.readLEInt();
		channelCount = in.readLEInt(); //static
		skeletonId = in.readLEInt();
		expect(in.readInt(), 0, "RW4-AN001", in.getFilePointer());  //< Usually zero.. always <= 10.  Maybe a count of something? // Not really, I think
		channelDataPos = in.readLEInt();
		paddingEndPos = in.readLEInt();
		expect(in.readLEInt(), channelCount, "RW4-AN002", in.getFilePointer());
		expect(in.readLEInt(), 0, "RW4-AN003", in.getFilePointer());
		length = in.readLEFloat();
		expect(in.readLEInt(), 12, "RW4-AN004", in.getFilePointer());
		flags = in.readLEInt();
		channelInfoPos = in.readLEInt();
		expect(channelNamesPos, in.getFilePointer(), "RW4-AN005", in.getFilePointer());
        expect(channelInfoPos, channelNamesPos + channelCount * 4, "RW4-AN006", in.getFilePointer());
        expect(channelDataPos, channelInfoPos + channelCount * 12, "RW4-AN007", in.getFilePointer());
        expect((paddingEndPos+15) & ~15, sections.get(this.sectionInfo.number+1).sectionInfo.pos , "RW4-AN008", in.getFilePointer());
        
        channels = new Channel[channelCount];
        for (int i = 0; i < channelCount; i++) {
        	channels[i] = new Channel();
        	channels[i].name = in.readLEInt();
        }
        for (int i = 0; i < channelCount; i++) {
        	channels[i].readInfo(in);
        }
        
        for (int i = 0; i < channelCount-1; i++) {
        	channels[i].keyframeCount = (channels[i+1].pos - channels[i].pos) / channels[i].pose_size;
        	channels[i].read(in);
        }
        
        // Last channel
        ArrayList<JointPose> jps = new ArrayList<JointPose>();
        JointPose jp = new JointPose();
        jp.read(in, channels[channelCount-1].pose_components);
        float time = jp.time;
        float oldtime = -1;
        channels[channelCount-1].keyframeCount = 0;
        while (time > oldtime) {
        	jps.add(jp);
        	oldtime = time;
        	
        	jp = new JointPose();
        	jp.read(in, channels[channelCount-1].pose_components);
        	time = jp.time;
        	
        	channels[channelCount-1].keyframeCount++;
        }
        int size = jps.size();
        channels[channelCount-1].poses = new JointPose[size];
        for (int i = 0; i < size; i++) {
        	channels[channelCount-1].poses[i] = jps.get(i);
        }
        
        //Determine the keyframes
//        ArrayList<JointPose> jps = new ArrayList<JointPose>();
//        JointPose jp = new JointPose();
//        jp.size = channels[0].pose_size;
//        jp.read(in);
//        float time = jp.time;
//        float oldtime = -1;
//        while (time > oldtime) {
//        	jps.add(jp);
//        	oldtime = time;
//        	
//        	jp = new JointPose();
//        	jp.read(in, channels[0].pose_size);
//        	time = jp.time;
//        	
//        	keyframeCount++;
//        }
//        int size = jps.size();
//        channels[0].poses = new JointPose[size];
//        for (int i = 0; i < size; i++) {
//        	channels[0].poses[i] = jps.get(i);
//        }
//        if (channelCount > 1) {
//	        channels[1].poses = new JointPose[keyframeCount];
//	        channels[1].poses[0] = jp;
//	        for (int i = 1; i < keyframeCount; i++) {
//	        	channels[1].poses[i] = new JointPose();
//	        	channels[1].poses[i].read(in, channels[1].pose_size);
//	        }
//	        
//	        for (int i = 2; i < channelCount; i++) {
//	        	channels[i].read(in, startingPos);
//	        }
//        }
        
//        padding = paddingEndPos-channelDataPos - channelCount*keyframeCount*channels[0].pose_size;//sections[this.sectionInfo.number+1].sectionInfo.pos - in.getFilePointer() + ();
//        in.expect(padding, channels[0].pose_size*2*keyframeCount*channelCount, "RW4-AN008; Unexpected padding: " + padding);
//        in.expect(in.getFilePointer()+padding+(((paddingEndPos+15)& ~15)-paddingEndPos), sections[this.sectionInfo.number+1].sectionInfo.pos, "RW4-AN008; Unexpected padding: " + padding);
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		int position = out.getFilePointer() + 12 * 4;
				
		out.writeLEInt(position); // channelNamesPos
		out.writeLEInt(channelCount); //static
		out.writeLEInt(skeletonId);
		out.writeInt(0);
		out.writeLEInt(position + channels.length * 4 + channels.length * 12); // channelDataPos
		out.writeLEInt(position + channels.length * 4 + channels.length * 12
                + channels.length * channels[0].poses.length
                * channels[0].pose_size + padding); // paddingEndPos //TODO Requires same number of keyframes, and at least one channel to exist
		out.writeLEInt(channelCount);
		out.writeInt(0);
		out.writeLEFloat(length);
		out.writeLEInt(12);
		out.writeLEInt(flags);
		out.writeLEInt(position + channels.length * 4); // channelInfoPos
		
		for (Channel c : channels) {
			out.writeLEInt(c.name);
		}
		for (Channel c : channels) {
			c.writeInfo(out);
		}
		for (Channel c : channels) {
			c.write(out);
		}
		out.write(new byte[padding]);
        
//        channels = new Channel[channelCount];
//        for (int i = 0; i < channelCount; i++) {
//        	channels[i] = new Channel();
//        	channels[i].name = in.readLEInt();
//        }
//        for (int i = 0; i < channelCount; i++) {
//        	channels[i].readInfo(in);
//        }
//        
//        for (int i = 0; i < channelCount-1; i++) {
//        	channels[i].keyframeCount = (channels[i+1].pos - channels[i].pos) / channels[i].pose_size;
//        	channels[i].read(in);
//        }
//        
//        // Last channel
//        ArrayList<JointPose> jps = new ArrayList<JointPose>();
//        JointPose jp = new JointPose();
//        jp.read(in, channels[channelCount-1].pose_components);
//        float time = jp.time;
//        float oldtime = -1;
//        channels[channelCount-1].keyframeCount = 0;
//        while (time > oldtime) {
//        	jps.add(jp);
//        	oldtime = time;
//        	
//        	jp = new JointPose();
//        	jp.read(in, channels[channelCount-1].pose_components);
//        	time = jp.time;
//        	
//        	channels[channelCount-1].keyframeCount++;
//        }
//        int size = jps.size();
//        channels[channelCount-1].poses = new JointPose[size];
//        for (int i = 0; i < size; i++) {
//        	channels[channelCount-1].poses[i] = jps.get(i);
//        }
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
        System.out.println("\tflags: " + flags);
    	System.out.println("\tlength: " + length);
    	System.out.println("\tskeleton id: " + Hasher.getFileName(skeletonId));
    	System.out.println("\tnum channels: " + channelCount);
    	System.out.println("\tpadding end pos: " + paddingEndPos);
    	for (Channel cn : channels) {
    		cn.print();
    	}
	}
	
	public class Channel {
		public int pos, p2, pose_size, pose_components, name, keyframeCount;
		public JointPose[] poses;
		public void readInfo(InputStreamAccessor in) throws IOException {
			pos = in.readLEInt();
			p2 = pos - (12*4 + channelCount*4 + channelCount*12);
        	pose_size = in.readLEInt();
        	pose_components = in.readLEInt(); // 0x101 rot loc sca; 0x601 rot loc ?; 0x100 strange
		}
		public void writeInfo(OutputStreamAccessor out) throws IOException {
			out.writeLEInt(pos);
			out.writeLEInt(pose_size);
			out.writeLEInt(pose_components);
		}
		public void read(InputStreamAccessor in) throws IOException {
//			in.expect(startingPos+this.p, in.getFilePointer(), "RW4-CHN001; Unexpected position: " + (startingPos+this.p) + "("+p+") instead of " + in.getFilePointer());
			poses = new JointPose[keyframeCount];
			for (int i = 0; i < keyframeCount; i++) {
				poses[i] = new JointPose();
				poses[i].read(in, pose_components);
			}
		}
		public void write(OutputStreamAccessor out) throws IOException {
			poses = new JointPose[keyframeCount];
			for (JointPose jp : poses) {
				jp.write(out, pose_components);
			}
		}
		public void print() {
			System.out.println("\t  " + Hasher.getFileName(name) + "\tkeyframe count: " + keyframeCount + "\tpos: " + pos + "\tp2: " + p2 + "\tsize: " + pose_size + "\tcomponents: " + Integer.toHexString(pose_components));
			for (JointPose pose : poses) {
				pose.print(pose_components);
			}
		}
	}
	public class JointPose {
		public float[] rotation = new float[4]; //Quaternion rotation
		public float[] translation = new float[3]; //Vector translation
		public float[] scale = new float[3]; //Vector scale
		public float time;
		public float blendFactor;
		public void read(InputStreamAccessor in, int components) throws IOException {
			if (components == 0x100) {
				blendFactor = in.readLEFloat();
			} else if (components == 0x101) {
				rotation[0] = in.readLEFloat();
				rotation[1] = in.readLEFloat();
				rotation[2] = in.readLEFloat();
				rotation[3] = in.readLEFloat();
				translation[0] = in.readLEFloat();
				translation[1] = in.readLEFloat();
				translation[2] = in.readLEFloat();
			} else if (components == 0x601) {
				rotation[0] = in.readLEFloat();
				rotation[1] = in.readLEFloat();
				rotation[2] = in.readLEFloat();
				rotation[3] = in.readLEFloat();
				translation[0] = in.readLEFloat();
				translation[1] = in.readLEFloat();
				translation[2] = in.readLEFloat();
				scale[0] = in.readLEFloat();
				scale[1] = in.readLEFloat();
				scale[2] = in.readLEFloat();
				expect(in.readInt(), 0, "RW4-JP001", in.getFilePointer());
			} else {
				addError("RW4-JP000", in.getFilePointer());
			}
			time = in.readLEFloat();
		}
		public void write(OutputStreamAccessor out, int components) throws IOException {
			if (components == 0x100) {
				out.writeLEFloat(blendFactor);
			} else if (components == 0x101) {
				out.writeLEFloats(rotation);
				out.writeLEFloats(translation);
			} else if (components == 0x601) {
				out.writeLEFloats(rotation);
				out.writeLEFloats(translation);
				out.writeLEFloats(scale);
				out.writeInt(0);
			}
			out.writeLEFloat(time);
		}
		public void print(int components) {
			if (components == 0x100) {
				System.out.println("\t\t" + blendFactor + "     " + time);
			} else if (components == 0x101) {
				System.out.println("\t\t["+rotation[0]+", "+rotation[1]+", "+rotation[2]+", "+rotation[3]+"]  "
						+ "  ["+translation[0]+", "+translation[1]+", "+translation[2]+"]     "+time);
			} else if (components == 0x601) {
				System.out.println("\t\t["+rotation[0]+", "+rotation[1]+", "+rotation[2]+", "+rotation[3]+"]  "
						+ "  ["+translation[0]+", "+translation[1]+", "+translation[2]+"]   "
								+ "  ["+scale[0]+", "+scale[1]+", "+scale[2]+"]     "+time);
			}
		}
	}
	
	@Override
	public int getSectionTypeCode() {
		return type_code;
	}
	@Override
	public int getSectionAlignment() {
		return alignment;
	}
}
