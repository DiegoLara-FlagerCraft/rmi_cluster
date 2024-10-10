package rmi;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

public class ClusterServiceImpl extends UnicastRemoteObject implements ClusterService {
    private final Queue<Job> algorithmQueue;

    public ClusterServiceImpl() throws RemoteException {
        super();
        algorithmQueue = new LinkedList<>();
    }

    @Override
    public synchronized String submitAlgorithm(String fileName, byte[] fileData, ClientCallback callback) throws RemoteException {
        String filePath = "/path/to/cluster/" + fileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);
            algorithmQueue.add(new Job(filePath, callback));
            new Thread(() -> processQueue()).start();
            return "Algorithm submitted successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error submitting algorithm.";
        }
    }

    private void processQueue() {
        while (!algorithmQueue.isEmpty()) {
            Job job = algorithmQueue.poll();
            String filePath = job.getFilePath();
            ClientCallback callback = job.getCallback();
            try {
                ProcessBuilder pb = new ProcessBuilder("mpirun", "-np", "4", "gcc", filePath);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
                callback.onResult(output.toString());

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                try {
                    callback.onResult("Error processing the algorithm: " + e.getMessage());
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
        }
    }

    private class Job {
        private final String filePath;
        private final ClientCallback callback;

        public Job(String filePath, ClientCallback callback) {
            this.filePath = filePath;
            this.callback = callback;
        }

        public String getFilePath() {
            return filePath;
        }

        public ClientCallback getCallback() {
            return callback;
        }
    }

    public static void main(String[] args) {
        try {
            ClusterServiceImpl server = new ClusterServiceImpl();
            Naming.rebind("ClusterService", server);
            System.out.println("RMI Server is running...");
        } catch (MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
    }
}
