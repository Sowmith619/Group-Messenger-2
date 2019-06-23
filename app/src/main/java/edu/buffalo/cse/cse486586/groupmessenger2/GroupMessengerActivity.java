package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    //Assign the port numbers for all the avd's
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static String sent="1";
    static String notsent="0";
    static List<ArrayList<String>> serverlist= new ArrayList<ArrayList<String>>();
    static HashMap<String,ArrayList<String>> maplist= new HashMap<String, ArrayList<String>>();
    ArrayList<String> clientlist=new ArrayList<String>();
    HashMap<String,String> hj=new HashMap<String, String>();
    static int self_incrementer=0;
    static int incrementer=0;
    static List<ArrayList<String>> buffer= new ArrayList<ArrayList<String>>();
    static ArrayList<String> portlist=new ArrayList<String>();
    static final int SERVER_PORT = 10000;
    static int port=0;
    static String  nowport="";
    static String portstring = "00000";
    static int mg=0;
    static String h="";
    Comparator<ArrayList<String>> comp= new Comparator<ArrayList<String>>() {


        @Override
        public int compare(ArrayList<String> p, ArrayList<String> q) {
            if (Integer.compare(Integer.parseInt(p.get(1)), Integer.parseInt(q.get(1))) == 0) {
              //  if (Integer.compare(Integer.parseInt(p.get(2)), Integer.parseInt(q.get(2))) == 0) {
                    return Integer.compare(Integer.parseInt(p.get(0)), Integer.parseInt(q.get(0)));
               // } else {
              //      return Integer.compare(Integer.parseInt(p.get(2)), Integer.parseInt(q.get(2)));
              //  }
            } else {
                return Integer.compare(Integer.parseInt(p.get(1)), Integer.parseInt(q.get(1)));
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);;


        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        final EditText textedit = (EditText) findViewById(R.id.editText1);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        portlist.add("11108");
        portlist.add("11112");
        portlist.add("11116");
        portlist.add("11120");
        portlist.add("11124");
//        hj.put("11108",portstring);
//        hj.put("11112",portstring);
//        hj.put("11116",portstring);
//        hj.put("11120",portstring);
//        hj.put("11124",portstring);
        final  String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        port=Integer.parseInt(myPort);
        Log.e(TAG,myPort);
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=textedit.getText().toString();
                textedit.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            String senderPort=msgs[1];
            try {
                String msgToSend = msgs[0];
                self_incrementer=self_incrementer+1;
                String mid=senderPort+String.valueOf(self_incrementer);
                clientlist.add(mid);
                String msgOnClient="false"+",,"+mid+",,"+msgToSend+",,"+senderPort;
                ArrayList<String> as = new ArrayList<String>();
                as.add(mid);
                as.add(String.valueOf(self_incrementer));
                as.add(String.valueOf(port));
                as.add(notsent);
                as.add(msgToSend);
                serverlist.add(as);
                for(int i=0;i<portlist.size();i++){
                    nowport=portlist.get(i);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(portlist.get(i)));


                    try {
                        DataOutputStream dos= new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(msgOnClient);

                        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        String   m = dis.readUTF();
                        Log.e(TAG,m+" : printing message in sock 1 !");
                        if(m!=null) {
                            if (m.equals("sock1") && !socket.isClosed()) {
                                socket.close();
                            } else {
                                String propFromServer[] = m.split(",,");
                                String midFromServer = propFromServer[0];
                                String propSeq = propFromServer[1];
                                String propServerPort = propFromServer[2];

                                if (maplist.get(midFromServer) == null) {
                                    ArrayList<String> a = new ArrayList<String>();
                                    a.add(propSeq + ",," + propServerPort);
                                    maplist.put(midFromServer, a);
                                } else {
                                    ArrayList<String> a = maplist.get(midFromServer);
                                    a.add(propSeq + ",," + propServerPort);
                                    maplist.put(midFromServer, a);
                                }


                            }
                        }
                       // socket.close();
                    }
                    catch(Exception e){
                        portstring=nowport;
                        Log.e(TAG,nowport+":  exception sock 1");
                        e.printStackTrace();
                        socket.close();
                        portlist.remove(nowport);
                        i=i-1;
                    }
                }
                if(clientlist.size()!=0 )
                    for(String s:clientlist) {
                        Log.e(TAG,s+" in s2");
                        int max = 0;
                        int accport = 0;
                        if(maplist.get(s)!=null)
                        if (maplist.get(s).size() >= portlist.size()) {
                            Log.e(TAG,"Above sock 2 "+ s +"..."+maplist.get(s).size());
                            for (String c : maplist.get(s)) {
                                Log.e(TAG,c+ "I m c in s2");
                                String split[] = c.split(",,");
                                if (Integer.parseInt(split[0]) >= max) {
                                    if (Integer.parseInt(split[0]) == max) {
                                        if (Integer.parseInt(split[1]) > accport) {
                                            accport = Integer.parseInt(split[1]);
                                        }
                                    } else {
                                        max = Integer.parseInt(split[0]);
                                        accport = Integer.parseInt(split[1]);
                                    }
                                }
                            }
                            Log.e(TAG,s+".."+max+"...."+accport);


                            ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>(serverlist);
                            for(int k=0;k<serverlist.size();k++) {
                            if(serverlist.get(k).get(0).equals(s)) {

                                maplist.get(s).clear();
                                int f=0;
                                for (int i = 0; i < portlist.size(); i++) {
                                    nowport = portlist.get(i);
                                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portlist.get(i)));

                                    try {
                                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                                        dos.writeUTF("true" + ",," + s + ",," + max + ",," + accport);
                                        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                                        String m = dis.readUTF();
                                        if (m != null) {

                                            if (m.contains("received")) {

                                                String received[] = m.split(",,");
                                                String mess = received[0];
                                                if (maplist.get(s) == null) {
                                                    ArrayList<String> a = new ArrayList<String>();
                                                    a.add(received[1]);
                                                    maplist.put(mess, a);
                                                } else {
                                                    ArrayList<String> a = maplist.get(s);
                                                    a.add(received[1]);
                                                    maplist.put(mess, a);
                                                }

                                                if (maplist.get(s).size() >= portlist.size()) {
                                                    f = 1;
                                                }
                                            } else {
                                                if (m.equals("sock2") && !socket.isClosed()) {
                                                    socket.close();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, nowport + ":  exception sock 2");
                                        e.printStackTrace();
                                        portstring = nowport;
                                        socket.close();
                                        portlist.remove(nowport);
                                        if (maplist.get(s) != null)
                                            if (maplist.get(s).size() >= portlist.size()) {
                                                f = 1;
                                            }
                                        i = i - 1;
                                    }
                                }

                                if (f == 1) {
                                    maplist.get(s).clear();
                                    for (int i = 0; i < portlist.size(); i++) {
                                        nowport = portlist.get(i);
                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portlist.get(i)));
                                        try {
                                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                                            dos.writeUTF("send,," + s + ",," + port);
                                            if (socket.isClosed() == false) {
                                                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                                                String m = dis.readUTF();
                                                if (m.equals("sock3") && !socket.isClosed()) {
                                                    socket.close();
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, nowport + ":  exception sock 3 " +s);
                                            e.printStackTrace();
                                            portstring = nowport;
                                            socket.close();
                                            portlist.remove(nowport);
                                            i = i - 1;
                                        }
                                    }

                                }
                            }
                            }
                        }
                    }

            } catch(Exception e){
                Log.e(TAG,"Exception "+nowport);
                e.printStackTrace();

            }

            return null;
        }
    }

    //Implementing server task
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private Uri AUri = null;
int f=0;


        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            String acktestmsg= null;
            AUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            Socket client = null;
            // PrintWriter server_ack=null;
            //we keep accepting messages from client so we use while(true)


                while (true) {
                    try{

                    if (serverlist.size() != 0) {

                        ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>(serverlist);
                            if(!portstring.equals("00000") && f!=1){
                                for (ArrayList a : al) {
                                    if (a.get(0).toString().contains(portstring) && a.get(3).equals(notsent)) {
                                        f=1;
                                        serverlist.remove(a);
                                    }
                                }
                            }
                            Thread.sleep(400);
                            Collections.sort(serverlist,comp);

                            for (ArrayList<String> a : serverlist) {
                                Log.e(TAG, a.toString());
                            }

                            Log.e(TAG, "......");

                            while (serverlist.size() != 0 && serverlist.get(0).get(3).equals(sent)) {
                                ContentValues keyValueToInsert = new ContentValues();
                                keyValueToInsert.put("key", incrementer++);
                                keyValueToInsert.put("value", serverlist.get(0).get(4));
                                Uri newUri = getContentResolver().insert(AUri, keyValueToInsert);

                                publishProgress(serverlist.get(0).get(4));
                                serverlist.remove(0);
                            }

                        }

                        client = serverSocket.accept();


                        acktestmsg = new DataInputStream(new BufferedInputStream(client.getInputStream())).readUTF();


                        if ((acktestmsg) != null && client != null) {

                            if (acktestmsg.contains("send")) {
                                String b[] = acktestmsg.split(",,");
                                h=b[2];
                                for (ArrayList<String> a : serverlist) {
                                    if (a.get(0).equals(b[1]) && a.get(3).equals(notsent) ){
                                        a.set(3, sent);
                                        Log.e(TAG,"status changed for: "+b[1]);


                                        break;
                                    }
                                }
                                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                                dos.writeUTF("sock3");


                            } else {
                                String msgFromClient[] = acktestmsg.split(",,");
                                String agreement = msgFromClient[0];
                                if ("false".equals(agreement)) {

                                    String messageId = msgFromClient[1];
                                    String actualMessage = msgFromClient[2];
                                    String senderPort = msgFromClient[3];
                                    h = senderPort;
                                    if (!senderPort.equals(port + "")) {
                                        self_incrementer = self_incrementer + 1;
                                        ArrayList<String> as = new ArrayList<String>();
                                        as.add(messageId);
                                        as.add(String.valueOf(self_incrementer));
                                        as.add(String.valueOf(port));
                                        as.add(notsent);
                                        as.add(actualMessage);
                                        serverlist.add(as);
                                    }

                                    DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                                    dos.writeUTF(messageId + ",," + String.valueOf(self_incrementer) + ",," + String.valueOf(port));

                                    dos.writeUTF("sock1");


                                } else if("true".equals(agreement)) {
                                    DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                                    String messageId = msgFromClient[1];
                                    String agreedSeq = msgFromClient[2];
                                    String senderPort = msgFromClient[3];
                                    h = senderPort;
                                    for (ArrayList<String> a : serverlist) {
                                        if (a.get(0).equals(messageId)) {
                                            if (Integer.parseInt(a.get(1)) <= Integer.parseInt(agreedSeq)) {
                                                a.set(1, agreedSeq);
                                                a.set(2, senderPort);
                                                dos.writeUTF(messageId + ",," + port + ",,received");

                                                break;
                                            }

                                        }
                                    }
                                    dos.writeUTF("sock2");
                                }
                            }
                        }
                    }catch (Exception f) {
                        Log.e(TAG,"caught in server: "+h);
                      if(portstring.equals("00000") && portlist.size()==5){

                          portstring=h;

                          portlist.remove(h);
                          Log.e(TAG, "caught in server Exception"+portlist.size());
                          try {
                              client.close();
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }

                    }


            finally {
                try {
                    if(client!=null)
                        client.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

}

        protected void onProgressUpdate(String...strings) {
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\n");
            return;

        }
    }
}