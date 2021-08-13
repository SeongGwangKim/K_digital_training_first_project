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
	
	// Ŭ���̾�Ʈ�κ��� �޽����� ���� �޴� �޼ҵ�
	public void receive() {
		Runnable thread = new Runnable() {
			// Runnable ���̺귯���� ���������� �ݵ�� run�Լ��� ������ �־���Ѵ�.
			// ��, �ϳ��� �����尡 ��� ���ν� ������ �Ұ��� �� run�ȿ��� ���Ǹ� ���ش�.
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
		// socket.getRemoteSocketAddress : ���� ������ �� Ŭ���̾�Ʈ�� IP�ּҿ� ���� �ּ� ������ ���
		// Thread.currentThread().getName() : �������� ������ ���� ���
						System.out.println("[�޽��� ���� ����]"
								+socket.getRemoteSocketAddress() +": "
								+ Thread.currentThread().getName());
						// UTF-8 : �ѱ۵� ����� �� �ֵ��� ���ڵ� ó��
						// ���ۿ��� ���޹��� �޼������ �̸��� ���ڿ� ������ ��Ƽ� ����� �� �ֵ��� �Ѵ�.
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
			// �Ϲ������� ��ø�� �������� try~catch ������ ����Ѵ�.
				}catch(Exception e) {
					try {
						System.out.println("[�޼��� ���� ����]"
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
	
	// Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�
	public void send(String message) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
				// getInputStream���� �ٸ� ��ǻ�ͷκ��� ��� ������ read�Լ��� �̿��ؼ� ������ �ް�
				// �޼����� �����ְ��� �Ҷ��� OutputStream�� �̿��ؼ� �޼����� �����Ѵ�.
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
				// ������ �߻����� �ʾ��� ��
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					try {
						System.out.println("[�޼��� �۽� ����]"
								+ socket.getRemoteSocketAddress() +": "
								+ Thread.currentThread().getName());
				// ������ �߻��ߴٸ� ���� �Լ��� �ִ� ��� Ŭ���̾�Ʈ�� ������ ��� �迭����
				// ���� �����ϴ� Ŭ���̾�Ʈ�� �����ش�.
				// ��, ������ �߻��ؼ� �ش� Ŭ���̾�Ʈ�� �����κ��� ������ �������ϱ�
				//�츮 ���� �ȿ����� �ش� Ŭ���̾�Ʈ�� ������ ����ٴ� ���� ó��(����)
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
