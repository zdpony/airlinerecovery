package sghku.tianchi.IntelligentAviation.common;

import java.io.*;

public class MyFile {
	private static String path = "C:\\";
	private static String filenameTemp;

	/**
	 * �����ļ�
	 * 
	 * @throws IOException
	 */
	public static boolean creatTxtFile(String name) throws IOException {
		boolean flag = false;
		//filenameTemp = name + ".txt";
		
		filenameTemp = name + "";
		System.out.println("filenameTemp:"+filenameTemp);
		File filename = new File(filenameTemp);
		if (!filename.exists()) {
			filename.createNewFile();
			flag = true;
		}
		return flag;
	}

	/**
	 * д�ļ�
	 * 
	 * @param newStr
	 *            ������
	 * @throws IOException
	 */
	public static boolean writeTxtFile(String newStr) throws IOException {
		// �ȶ�ȡԭ���ļ����ݣ�Ȼ�����д�����
		boolean flag = false;
		String filein = newStr + "\r\n";
		String temp = "";

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			// �ļ�·��
			File file = new File(filenameTemp);
			// ���ļ�����������
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			StringBuffer buf = new StringBuffer();

			// ������ļ�ԭ�е�����
			for (int j = 1; (temp = br.readLine()) != null; j++) {
				buf = buf.append(temp);
				// System.getProperty("line.separator")
				// ������֮��ķָ��� �൱�ڡ�\n��
				buf = buf.append(System.getProperty("line.separator"));
			}
			buf.append(filein);

			fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
			pw.write(buf.toString().toCharArray());
			pw.flush();
			flag = true;
		} catch (IOException e1) {
			// TODO �Զ����� catch ��
			throw e1;
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
		return flag;
	}
	
	public static void setFileName(String fileName){
		filenameTemp = fileName;
	}

}
