package sporemodder.files.formats.snr;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.StreamAccessor;

public class SNRMain {
	public void read(InputStreamAccessor in) throws IOException {
		//TODO Add support for SNR and SNS files
		int identifier = in.readByte();
		
		if (identifier == 4) { //FFMPEG
			
		}
	}
}
