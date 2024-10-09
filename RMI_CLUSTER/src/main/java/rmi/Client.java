package rmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;

public class Client {

    public static void main(String[] args) {
        try {
            ClusterService service = (ClusterService) Naming.lookup("rmi://localhost/ClusterService");
            ClientCallback callback = new ClientCallbackImpl();
            String filePath = "path/to/your/algorithm.c";
            byte[] fileData = readFile(filePath);
            String response = service.submitAlgorithm("algorithm.c", fileData, callback);
            System.out.println("Server Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }
        return fileData;
    }
}
