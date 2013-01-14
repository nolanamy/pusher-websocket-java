package com.pusher.client.channel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.ChannelState;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Factory.class})
public class PrivateChannelImplTest extends ChannelImplTest {

    private @Mock InternalConnection mockConnection;
    
    @Test
    public void testConstructWithNonPrivateChannelNameThrowsException() {
	
	String[] invalidNames = new String[] {"my-channel", "private:my-channel", "Private-my-channel", "privatemy-channel"};
	for(String invalidName : invalidNames) {
	    try {
		newInstance(invalidName);
		fail("No exception thrown for invalid name: " + invalidName);
	    } catch(IllegalArgumentException e) {
		// exception correctly thrown
	    }
	}
    }

    @Test
    @Override
    public void testConstructWithPrivateChannelNameThrowsException() {
	// overridden because this test is not valid for this class - we don't want to throw an exception
    }    

    @Test
    @Override
    public void testReturnsCorrectSubscribeMessage() {
	String authResponse = "{\"auth\":\"appKey:1234567\"}";
	assertEquals("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"" + getChannelName() + "\",\"auth\":\"appKey:1234567\"}}", channel.toSubscribeMessage(authResponse));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testToSubscriptionMessageWithNoArgumentsThrowsException() {
	channel.toSubscribeMessage();
    }

    @Test
    public void testTriggerWithValidEventSendsMessage() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.SUBSCRIBED);
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":\"chips\"}");
	
	verify(mockConnection).sendMessage("{\"event\":\"client-myEvent\",\"channel\":\"" + getChannelName() + "\",\"data\":{\"fish\":\"chips\"}}");
    }
 
    @Test(expected=IllegalArgumentException.class)
    public void testTriggerWithNullEventNameThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.SUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger(null, "{\"fish\":\"chips\"}");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testTriggerWithInvalidEventNameThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.SUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger("myEvent", "{\"fish\":\"chips\"}");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testTriggerWithInvalidJSONThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.SUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":malformed");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testTriggerWithJSONStringInsteadOfObjectThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.SUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "string");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testTriggerWhenChannelIsInInitialStateThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":\"chips\"}");
    }

    @Test(expected=IllegalStateException.class)
    public void testTriggerWhenChannelIsInSubscribeSentStateThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.SUBSCRIBE_SENT);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":\"chips\"}");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testTriggerWhenChannelIsInUnsubscribedStateThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
	channel.updateState(ChannelState.UNSUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":\"chips\"}");
    }
    
    @Test(expected=IllegalStateException.class) 
    public void testTriggerWhenConnectionIsInDisconnectedStateThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);
	channel.updateState(ChannelState.SUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":\"chips\"}");
    }
    
    @Test(expected=IllegalStateException.class) 
    public void testTriggerWhenConnectionIsInConnectingStateThrowsException() {
	when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);
	channel.updateState(ChannelState.SUBSCRIBED);
	
	((PrivateChannelImpl)channel).trigger("client-myEvent", "{\"fish\":\"chips\"}");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCannotBindIfListenerIsNotAPrivateChannelEventListener() {
	ChannelEventListener listener = mock(ChannelEventListener.class);
	channel.bind("private-myEvent", listener);
    }
    
    /* end of tests */

    @Override
    protected ChannelImpl newInstance(String channelName) {
	return new PrivateChannelImpl(mockConnection, channelName);
    }

    @Override
    protected String getChannelName() {
	return "private-my-channel";
    }
    
    protected ChannelEventListener getEventListener() {
	PrivateChannelEventListener listener = mock(PrivateChannelEventListener.class);
	return listener;
    }
}