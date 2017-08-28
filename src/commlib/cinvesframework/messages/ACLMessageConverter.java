package commlib.cinvesframework.messages;

import java.io.*;

public class ACLMessageConverter {

    public static byte[] objectToBytes(Object object) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        try {

            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();

            byte[] objectBytes = bos.toByteArray();

            return objectBytes;

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static <T extends Object> T bytesToObject(byte objectBytes[], Class<T> type) {

        ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
        ObjectInput in = null;

        try {

            in = new ObjectInputStream(bis);
            Object object = in.readObject();

            return type.cast(object);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

}
