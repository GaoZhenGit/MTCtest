# MTC消息通信框架使用文档
### 项目简介
本消息通信框架取名MTC，全称Message Transfer Center,使用发布订阅的方式，应用于Android系统中，各个模块之间的消息通信。能够跨线程，跨进程。
### 使用方法
##### 初始化
框架需要进行初始化。使用MTCManager来进行初始化,最好在项目自定义的Application中调用该方法：
```
public static void init(Application app, Map<String, Class<? extends RemoteService>> remoteServices)
```
该方法有两个参数，第一个为Application的实例。第二个是一个Map。该Map是进程名作为key，以及相应的进程的Service类作为value。该Service应该继承`RemoteService`，并在Manifest中标注进程。
例子：该项目有一个：remote进程，所以在manifest中注册：
```
 <service
	android:name=".MyService"
	android:enabled="true"
	android:exported="true"
	android:process=":remote" />
```
然后在Application中调用init方法，将进程对应Map写入：
```
 Map<String,Class<? extends RemoteService>> map = new HashMap<>();
 map.put("com.gz.imtc:remote", MyService.class);/*com.ga.imtc:remote为远程进程名*/
 MTCManager.init(this,map);
```
至此，框架初始化完毕。
##### 消息载体
作为通信的消息载体，使用了`IMessgae`接口，可以跨进程发送消息。实现类为`Message`。其中变量mid为发布消息和订阅消息的订阅号，作为发送和接收的地址，使用Android中的Bundle作为消息携带的数据载体。
消息接口：
```
interface IMessage {
    void setMid(String mid);
    String getMid();
    void setPayload(inout Bundle bundle);
    Bundle getPayload();
}
```
##### 注册
作为接受消息的订阅者模块，需要向框架订阅响应的主题，才能接受到框架的消息。
框架提供了两种接口，一种是广播消息接口`MutiMsgReceiver`,另一种是独立消息接口`UniqueMsgReceiver`。前者代表可能有多个消息接收方，一对多，并无返回结果。后者为独立的一对一消息，并有同步和异步返回结果选择。
广播消息接口：
```
public interface MutiMsgReceiver {
    void onReceive(IMessage message);/*在线程池中的线程执行*/
}
```
独立消息接口：
```
public interface UniqueMsgReceiver {
	/*该方法在发送方异步请求时调用，在回调接口中将结果作为参数输入。该方法在非UI线程调用*/
    void onAsynReceive(IMessage message, IMsgCallback msgCallback);
    /*该方法在发送方同步请求时调用，结果以返回值形式返回。该方法在非UI线程调用，发送方在等待该方法结束*/
    Bundle onSynReceive(IMessage message);
}
```
注册广播消息例子：
```
mutiMsgReceiver = new MutiMsgReceiver() {
    @Override
    public void onReceive(IMessage message) {
        try {
            Log.i("mainAct","receive:" + message.getMid());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
};
MTCManager.getMTC().register("mut3",mutiMsgReceiver);
//注册了一个订阅号为"mut3"的广播消息
```
注册独立消息例子：
```
uniqueMsgReceiver = new UniqueMsgReceiver() {
	@Override
	public void onAsynReceive(IMessage message, IMsgCallback msgCallback) {
		try {
            Log.i("localAct", "bundle:" + message.getPayload().getString("key"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Bundle bundle = new Bundle();
		bundle.putString("key", "loc->loc,asyn");
		if (msgCallback != null) {
            try {
                msgCallback.onComplete(bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    	}
    }

    @Override
    public Bundle onSynReceive(IMessage message) {
    	Bundle bundle = new Bundle();
    	bundle.putString("key", "loc->loc,syn");
    	return bundle;
    }
};
MTCManager.getMTC().registerUnique("rec1", uniqueMsgReceiver);
```
##### 发布消息
发布模块首先需要准备一个发布的消息`Message`,设置好发送订阅号mid，以及信息载体。
发送消息接口：
```
void sendMutiMessage(IMessage message);//发送本进程广播消息，没有返回结果，异步发送

Bundle sendUniqueMessage(IMessage message);//发送本进程独立消息，同步等待

void sendUniqueMessage(IMessage message, IMsgCallback callback);//发送本进程独立消息，异步回调

void sendMutiIPCMessage(IMessage message);//发送跨进程广播消息，全局发送，无返回结果

void sendMutiIPCMessage(IMessage message, String processName);//发送跨指定进程广播消息，无返回结果

Bundle sendUniqueIPCMessage(IMessage message);//发送独立消息，在全进程中寻找接受方，同步等待结果

Bundle sendUniqueIPCMessage(IMessage message, String processName);//发送独立消息，仅在指定进程中寻找接受方，同步等待结果

void sendUniqueIPCMessage(IMessage message, IMsgCallback callback);//发送独立消息，在全进程中寻找接受方,异步等待结果

void sendUniqueIPCMessage(IMessage message, String processName, IMsgCallback msgCallback);//发送独立消息，仅在指定进程中寻找接受方，异步等待结果
```