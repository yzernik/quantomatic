package quanto.gui;

import java.io.*;
import java.util.Set;

import edu.uci.ics.jung.contrib.HasName;

/**
 * Regulate communications with the back-end. Primarily accessed via wrappers
 * to the "command" method, which throw QuantoCore.ConsoleError.
 * 
 * In this version, the core contains no GUI code.
 * @author aleks kissinger
 *
 */
public class QuantoCore {
 
	public static final int VERTEX_RED = 1;
	public static final int VERTEX_GREEN = 2;
	public static final int VERTEX_HADAMARD = 3;
	public static final int VERTEX_BOUNDARY = 4;
	
	Process backEnd;
	BufferedReader from_backEnd;
	BufferedReader from_backEndError;
	BufferedWriter to_backEnd;
	PrintStream output;
	
	public static class ConsoleError extends Exception {
		private static final long serialVersionUID = 1053659906558198953L;
		public ConsoleError(String msg) {
			super(msg);
		}
	}
	
	public static class FatalError extends RuntimeException {
		private static final long serialVersionUID = -3757849807264018024L;
		public FatalError(String msg) {
			super(msg);
		}
		
		public FatalError(Exception e) {
			super(e);
		}
	}

	public QuantoCore(PrintStream output) {
		this.output = output;
		try {
			ProcessBuilder pb = new ProcessBuilder("quanto-core");	
			System.out.println("Initialising QuantoML...");
			backEnd = pb.start();
			
			System.out.println("Connecting pipes...");
			from_backEnd = new BufferedReader(new InputStreamReader(backEnd
					.getInputStream()));
			from_backEndError = new BufferedReader(new InputStreamReader(backEnd
					.getErrorStream()));
			to_backEnd = new BufferedWriter(new OutputStreamWriter(backEnd
					.getOutputStream()));
			
			System.out.println("Synchonising console...");
			// sync the console
			send("garbage_2039483945;");
			send("HELO;");
			while (!receive().contains("HELO"));
			System.out.println("done.");
		} catch (IOException e) {
			e.printStackTrace();
			if(backEnd == null) { output.println("ERROR: Cannot execute: quanto-core, check it is in the path."); }
			else {output.println("Exit value from backend: " + backEnd.exitValue()); }
		}
	}

	public void send(String command) {
		if(to_backEnd != null){
			try {
				to_backEnd.write(command);
				to_backEnd.newLine();
				to_backEnd.flush();
			} catch (IOException e) {
				output.println("Exit value from backend: "
						+ backEnd.exitValue());
				e.printStackTrace();
			}
		}
	}

	public String receive() {
		StringBuffer message = new StringBuffer();
		try {
			// end of text is marked by " "+BACKSPACE (ASCII 8)
			
			int c = from_backEnd.read();
			while (c != 8) {
				message.append((char)c);
				c = from_backEnd.read();
			}
			
			// delete the trailing space
			message.deleteCharAt(message.length()-1);
		} catch (IOException e) {
			output.println("Exit value from backend: " + backEnd.exitValue());
			e.printStackTrace();
		}
		catch (java.lang.NullPointerException e) {
			output.println("Exit value from backend: " + backEnd.exitValue());
			e.printStackTrace();
			return null;
		}
		
		return message.toString();
	}
	
	public String receiveOrFail() throws ConsoleError {
		String rcv = receive();
		
		if (rcv.startsWith("!!!")) {
			throw new ConsoleError(rcv.substring(4));
		}
		return rcv;
	}
	
	public void closeQuantoBackEnd(){
		output.println("Shutting down quantoML");
		send("quit");
	}
	
	
	/*
	 * Some helpers for the methods below
	 */
	
	/**
	 * Send a command with the given arguments. All of the args should be of types
	 * with a well-behaved toString() method.
	 */
	protected String command(String name, HasName ... args) throws ConsoleError {
		StringBuffer cmd = new StringBuffer(name);
		for (HasName arg : args) {
			if (arg.getName() == null)
				throw new ConsoleError(
						"Attempted to pass unnamed object to core.");
			cmd.append(' ');
			cmd.append(arg.getName());
		}
		cmd.append(';');
		
		String ret;
		System.out.print(cmd);
		synchronized (this) {
			send(cmd.toString());
			try {
				ret = receiveOrFail();
			} finally {
				receive(); // eat the prompt
			}
		}
		
		return ret;
	}
	
	/**
	 * Remove all line breaks.
	 */
	protected String chomp(String str) {
		return str.replace("\\n", "");
	}
	
	/*
	 * Below here are all the functions implemented by the quanto core
	 */
	
	
	public String graph_xml(QuantoGraph graph) throws ConsoleError {
		return command("graph_xml", graph);
	}
	
	public QuantoGraph new_graph() throws ConsoleError {
		return new QuantoGraph(chomp(command("new_graph")));
	}
	
	public void add_vertex(QuantoGraph graph, QVertex.Type type)
	throws ConsoleError {
		command("add_vertex", graph, 
				new HasName.StringName(type.toString().toLowerCase()));
	}
	
	public void add_edge(QuantoGraph graph, QVertex s, QVertex t)
	throws ConsoleError {
		command("add_edge", graph, s, t);
	}
	
	public void attach_rewrites(QuantoGraph graph, Set<QVertex> vs)
	throws ConsoleError {
		command("attach_rewrites", graph, new HasName.SetName(vs));
	}
	
	public String show_rewrites(QuantoGraph graph) throws ConsoleError {
		return command("show_rewrites", graph);
	}
	
	public void apply_rewrite(QuantoGraph graph, int i) throws ConsoleError {
		command("apply_rewrite", graph, new HasName.IntName(i));
	}
	
	public void set_angle(QuantoGraph graph, QVertex v, String angle) throws ConsoleError {
		command("set_angle", graph, v, new HasName.StringName(angle));
	}
	
	public String hilb(QuantoGraph graph, String format) throws ConsoleError {
		return command("hilb", graph, new HasName.StringName(format));
	}
	
	public void delete_vertex(QuantoGraph graph, QVertex v) throws ConsoleError {
		command("delete_vertex", graph, v);
	}
	
	public void delete_edge(QuantoGraph graph, QEdge e) throws ConsoleError {
		command("delete_edge", graph, e);
	}
	
	public void undo(QuantoGraph graph) throws ConsoleError {
		command("undo", graph);
	}
	
	public void redo(QuantoGraph graph) throws ConsoleError {
		command("redo", graph);
	}
	
	public void save_graph(QuantoGraph graph, String fileName) throws ConsoleError{
		command("save_graph", graph, new HasName.StringName(fileName));
	}
	public QuantoGraph load_graph(String fileName) throws ConsoleError{
		return new QuantoGraph(chomp(command("load_graph", new HasName.StringName(fileName))));
	}
	
}