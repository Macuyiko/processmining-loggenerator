package org.processmining.plugins.kutoolbox.utils;


import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;

public class FakePluginContext extends UIPluginContext {
	private static UIContext MAIN_CONTEXT = new UIContext();
	private static UIPluginContext MAIN_PLUGINCONTEXT = MAIN_CONTEXT.getMainPluginContext().createChildContext("");
	 
	public FakePluginContext() {
		this(MAIN_PLUGINCONTEXT, "Fake Plugin Context");
	}

	public FakePluginContext(UIPluginContext context, String label) {
		super(context, label);
	}

	public FakePluginContext(PluginContext context) {
		this(MAIN_PLUGINCONTEXT, "Fake Plugin Context");
		for (ConnectionID cid : context.getConnectionManager().getConnectionIDs()) {
			try {
				Connection connection = context.getConnectionManager().getConnection(cid);
				this.addConnection(connection);
			} catch (ConnectionCannotBeObtained e) {
			}
		}
	}

	@Override
	public Progress getProgress() {
		return new FakeProgress();
	}

	@Override
	public ProMFuture<?> getFutureResult(int i) {
		return new ProMFuture<Object>(String.class, "Fake Future") {
			@Override
			protected Object doInBackground() throws Exception {
				return new Object();
			}
		};
	}

	@Override
	public void setFuture(PluginExecutionResult futureToBe) {
		
	}
	
}
