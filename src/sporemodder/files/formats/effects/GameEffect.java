package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class GameEffect extends EffectComponent {
	
	public static final int TYPE = 0x0B;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "game";
	
	private int flags;  // 0x08  // & 0x3FF ?
	private int messageID;  // 0x0C
	private final int[] messageData = new int[4];  // 0x10
	private String messageString = "";  // 0x20
	private float life;  // 0x30

	public GameEffect(int type, int version) {
		super(type, version);
	}

	public GameEffect(GameEffect effect) {
		super(effect);

		flags = effect.flags;
		messageID = effect.messageID;
		messageData[0] = effect.messageData[0];
		messageData[1] = effect.messageData[1];
		messageData[2] = effect.messageData[2];
		messageData[3] = effect.messageData[3];
		messageString = new String(effect.messageString);
		life = effect.life;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		messageID = in.readInt();
		in.readInts(messageData);
		messageString = in.readCString();
		life = in.readFloat();
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeInt(messageID);
		out.writeInts(messageData);
		out.writeCString(messageString);
		out.writeFloat(life);
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		throw new UnsupportedOperationException(KEYWORD + " effect only supports inline version.");
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		throw new UnsupportedOperationException(KEYWORD + " effect only supports inline version.");
	}
	
	@Override
	public boolean supportsBlock() {
		return false;
	}

	@Override
	public void parseInline(ArgScriptCommand c) throws ArgScriptException {
		String arg;
		List<String> args;
		if ((arg = c.getOptionArg("id")) != null) 
		{
			messageID = Hasher.getFileHash(arg);
		}
		if ((arg = c.getOptionArg("life")) != null) 
		{
			life = Float.parseFloat(arg);
		}
		if ((arg = c.getOptionArg("string")) != null) 
		{
			messageString = arg;
		}
		if ((args = c.getOptionArgs("data", 4)) != null) 
		{
			messageData[0] = Hasher.decodeInt(args.get(0));
			messageData[1] = Hasher.decodeInt(args.get(1));
			messageData[2] = Hasher.decodeInt(args.get(2));
			messageData[3] = Hasher.decodeInt(args.get(3));
		}
		if ((arg = c.getOptionArg("flags")) != null) 
		{
			flags = Hasher.decodeInt(arg);
		}
	}

	@Override
	public ArgScriptCommand toCommand() {
		
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD, new String[0]);
		
		c.putOption(new ArgScriptOption("id", Hasher.getFileName(messageID)));
		if (life != 0) {
			c.putOption(new ArgScriptOption("life", Float.toString(life)));
		}
		if (messageData[0] != 0 || messageData[1] != 0 || messageData[2] != 0 || messageData[3] != 0) {
			c.putOption(new ArgScriptOption("data", "0x" + Integer.toHexString(messageData[0]),
					"0x" + Integer.toHexString(messageData[1]),
					"0x" + Integer.toHexString(messageData[2]),
					"0x" + Integer.toHexString(messageData[3])));
		}
		if (messageString != null && messageString.length() > 0) {
			c.putOption(new ArgScriptOption("string", messageString));
		}
		
		if (flags != 0) {
			c.putOption(new ArgScriptOption("flags", "0x" + Integer.toHexString(flags)));
		}
		
		return c;
	}
	
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"id", "life", "data", "string", "flags"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			KEYWORD
		};
	}
}
