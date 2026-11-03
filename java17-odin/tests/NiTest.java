import java.net.*;
import java.util.*;
public class NiTest { public static void main(String[] a) throws Exception {
  for (NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) System.out.println(n.getName()+" "+n.getDisplayName()+" up="+n.isUp()+" "+Collections.list(n.getInetAddresses()));
  System.out.println("NI OK"); System.exit(0);} }
