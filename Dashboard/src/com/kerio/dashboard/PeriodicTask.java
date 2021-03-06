package com.kerio.dashboard;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

// TODO: Consider using ThreadPoolExecutor or AsyncTask or other standard mechanism
abstract public class PeriodicTask implements Runnable
{
	
	private static final int DELAY = 20000;
	private class Helper implements Runnable{
		private Handler handler;
		private long delay;
		private Runnable job;
		
		public Helper(Runnable job, Handler handler, long delay)
		{
			this.job = job;
			this.handler = handler;
			this.delay = delay;
		}
		
		@Override
		public void run() {
			handler.post(new Runnable(){
				public void run(){
					try{
						Thread thread = new Thread(job);
						thread.start();
					}catch(Exception e){
						System.out.println(e.toString());
					}
				}
			});
		}
		
		public void reschedule() {
			if (this.delay != 0) {
				this.handler.postDelayed(this, this.delay);
			}
		}
	}
	
	private Helper helper;
	private Handler handler;
	private Handler initHandler;
	
	public PeriodicTask(Handler handler)
	{
		this.helper = new Helper(this, handler, DELAY);
		this.handler = handler;
		this.initHandler = handler;
	}
	
	public PeriodicTask(Handler handler, long delay)
	{
		this.helper = new Helper(this, handler, delay);
		this.handler = handler;
		this.initHandler = handler;
	}
	
	synchronized public void activate()
	{
		if (this.initHandler != null) {
			this.initHandler.post(this.helper);
			this.initHandler = null;
		}
	}
	
	synchronized public void deactivate()
	{
		if (this.initHandler == null) {
			this.initHandler = this.handler;
			this.initHandler.removeCallbacks(this.helper);
		}
	}
	
	public Handler getHandler() { return this.handler; }
	
	protected void notify(Object info) {
		Message msg = new Message();
		msg.obj = info;
	 	this.handler.sendMessage(msg);   	
	}

	@Override
	final public void run() {
		try {
			this.execute();
		}
		catch (Exception e) {
			Log.d("PeriodicTask", String.format("execution failed ('%s')", e.getMessage()));
		}
		
		this.helper.reschedule();
	}
	
	abstract public void execute();
}