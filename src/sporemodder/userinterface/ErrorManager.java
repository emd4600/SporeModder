package sporemodder.userinterface;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorManager {
	
	private static final int MAX_STACKTRACE = 6;
	
	public static String getStackTraceString(Throwable e) {
	    return getStackTraceString(e, "");
	}

	private static String getStackTraceString(Throwable e, String indent) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(e.toString());
	    sb.append("\n");

	    StackTraceElement[] stack = e.getStackTrace();
	    if (stack != null) {
	        for (int i = 0; i < MAX_STACKTRACE && i < stack.length; i++) {
	        	StackTraceElement stackTraceElement = stack[i];
	            sb.append(indent);
	            sb.append("\tat ");
	            sb.append(stackTraceElement.toString());
	            sb.append("\n");
	        }
	    }

	    Throwable[] suppressedExceptions = e.getSuppressed();
	    // Print suppressed exceptions indented one level deeper.
	    if (suppressedExceptions != null) {
	        for (Throwable throwable : suppressedExceptions) {
	            sb.append(indent);
	            sb.append("\tSuppressed: ");
	            sb.append(getStackTraceString(throwable, indent + "\t"));
	        }
	    }

	    Throwable cause = e.getCause();
	    if (cause != null) {
	        sb.append(indent);
	        sb.append("Caused by: ");
	        sb.append(getStackTraceString(cause, indent));
	    }

	    return sb.toString();
	}

	public static String getStringFromException(Exception ex) {
	    StringWriter errors = new StringWriter();
	    ex.printStackTrace(new PrintWriter(errors));
	    
	    return errors.toString();
	}
}
