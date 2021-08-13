package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	// 클라이언트로부터 메시지를 전달 받는 메소드
	public void receive() {
		Runnable thread = new Runnable() {
			// Runnable 라이브러리는 내부적으로 반드시 run함수를 가지고 있어야한다.
			// 즉, 하나의 스레드가 어떠한 모듈로써 동작을 할건지 이 run안에서 정의를 해준다.
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
		// socket.getRemoteSocketAddress : 현재 접속을 한 클라이언트의 IP주소와 같은 주소 정보를 출력
		// Thread.currentThread().getName() : 스레드의 고유한 정보 출력
						System.out.println("[메시지 수신 성공]"
								+socket.getRemoteSocketAddress() +": "
								+ Thread.currentThread().getName());
						// UTF-8 : 한글도 사용할 수 있도록 인코딩 처리
						// 버퍼에서 전달받은 메세지라는 이름의 문자열 변수에 담아서 출력할 수 있도록 한다.
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
			// 일반적으로 중첩된 형식으로 try~catch 구문을 사용한다.
				}catch(Exception e) {
					try {
						System.out.println("[메세지 수신 오류]"
								+ socket.getRemoteSocketAddress() +": "
								+ Thread.currentThread().getName());
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
	}
	
	// 클라이언트에게 메시지를 전송하는 메소드
	public void send(String message) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
				// getInputStream으로 다른 컴퓨터로부터 어떠한 내용을 read함수를 이용해서 전달을 받고
				// 메세지를 보내주고자 할때는 OutputStream을 이용해서 메세지를 전달한다.
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
				// 오류가 발생하지 않았을 때
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					try {
						System.out.println("[메세지 송신 오류]"
								+ socket.getRemoteSocketAddress() +": "
								+ Thread.currentThread().getName());
				// 오류가 발생했다면 메인 함수에 있는 모든 클라이언트의 정보를 담는 배열에서
				// 현재 존재하는 클라이언트를 지워준다.
				// 즉, 오류가 발생해서 해당 클라이언트가 서버로부터 접속이 끊겼으니까
				//우리 서버 안에서도 해당 클라이언트가 접속이 끊겼다는 것을 처리(제거)
						Main.clients.remove(Client.this);
						socket.close();
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
	}

}
