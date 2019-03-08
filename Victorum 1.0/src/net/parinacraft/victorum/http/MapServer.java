package net.parinacraft.victorum.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.bukkit.scheduler.BukkitRunnable;

import net.parinacraft.victorum.Victorum;

/**
 * The purpose of this class is to create an online interface for Victorum claim
 * map.
 */
public class MapServer {
	private Victorum pl;

	public MapServer(Victorum pl) {
		this.pl = pl;
	}

	public void startAsync() {
		BukkitRunnable br = new BukkitRunnable() {
			private ServerSocket httpServer;

			@Override
			public void run() {
				try {
					if (httpServer == null)
						httpServer = new ServerSocket();
					httpServer.bind(new InetSocketAddress(pl.getConfig().getInt("map-server-port")));
					while (!httpServer.isClosed()) {
						HTTPRequestHandler handler = new HTTPRequestHandler(httpServer.accept());
						handler.runTaskAsynchronously(pl);
						Thread.sleep(100);
					}
				} catch (SocketException e) {
					System.err.println("MapServer or something else already running on selected port!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public synchronized void cancel() throws IllegalStateException {
				Thread.currentThread().interrupt();
			}
		};
		br.runTaskAsynchronously(pl);

	}
}

class HTTPRequestHandler extends BukkitRunnable {

	private final PrintWriter bos;
	private final BufferedReader ios;
	private final Socket sock;

	public HTTPRequestHandler(Socket sock) throws IOException {
		this.bos = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
		this.ios = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		this.sock = sock;
	}

	// Read HTML request
	@Override
	public void run() {
		try {
			String line;
			int x = 0, z = 0;
			while ((line = ios.readLine()) != null) {
				if (line.startsWith("GET ")) {
					try {
						line = line.split(" ")[1];
						String params = line.split("\\?")[1];
						String[] args = params.split("&");
						x = Integer.parseInt(args[0].split("x=")[1]);
						z = Integer.parseInt(args[1].split("z=")[1]);
					} catch (Exception e) {
						System.err.println("Invalid request: " + line);
						sock.close();
						return;
					}
					break;
				}
			}
			bos.println("HTTP/1.1 200 OK");
			bos.println("Connection: Closed");
			bos.println("Content-Type: text/html");
			bos.println();
			bos.println();
			bos.println("<html><body><center><h1>Coordinates: " + x + ", " + z + "<h2></center></body></html>");
			bos.flush();
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}