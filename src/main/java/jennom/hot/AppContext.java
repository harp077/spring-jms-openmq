package jennom.hot;

import com.google.gson.Gson;
import com.sun.messaging.BasicConnectionFactory;
import com.sun.messaging.ConnectionConfiguration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
//
import org.springframework.jms.annotation.EnableJms;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import jennom.jms.JmsExceptionListener;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import com.sun.jndi.fscontext.RefFSContextFactory;

@Configuration
@ComponentScan(basePackages = {"jennom"})
@EnableAsync
@EnableJms
public class AppContext {
    
    private String concurrency = "1-8";
    private String brokerURL = "tcp://localhost:7676";
    
    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }   
    
    //=========== new 21-11-2021
    
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(JmsExceptionListener jmsExceptionListener) {
    	DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
	factory.setConnectionFactory(jmsConnectionFactory(jmsExceptionListener));
	factory.setDestinationResolver(destinationResolver());
	factory.setConcurrency(concurrency);
	factory.setPubSubDomain(false);
	return factory;
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory(JmsExceptionListener jmsExceptionListener) {
    	return createJmsConnectionFactory(jmsExceptionListener);
    }

    private ConnectionFactory createJmsConnectionFactory(JmsExceptionListener jmsExceptionListener) {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        env.put(Context.PROVIDER_URL, "file:///j/webapp/openmq-5.1.2/data");
        com.sun.messaging.ConnectionFactory bcf=null;
        try {
            Context ctx = new InitialContext(env);
            bcf = (com.sun.messaging.ConnectionFactory) ctx.lookup("ConnectionFactory");
        } catch (NamingException ex) {
            Logger.getLogger(AppContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            //ConnectionFactory bcf = new ConnectionFactory(brokerURL);
            //bcf.setExceptionListener(jmsExceptionListener);
            bcf.setProperty(ConnectionConfiguration.imqDefaultPassword, "guest");
            bcf.setProperty(ConnectionConfiguration.imqDefaultUsername, "guest");
        } catch (JMSException ex) {
            Logger.getLogger(AppContext.class.getName()).log(Level.SEVERE, null, ex);
        }
	CachingConnectionFactory connectionFactory = new CachingConnectionFactory(bcf);
        connectionFactory.setExceptionListener(jmsExceptionListener);
	return connectionFactory;
    }

	@Bean(name = "jmsQueueTemplate")
	public JmsTemplate createJmsQueueTemplate(ConnectionFactory jmsConnectionFactory) {
		return new JmsTemplate(jmsConnectionFactory);
	}

	@Bean(name = "jmsTopicTemplate")
	public JmsTemplate createJmsTopicTemplate(ConnectionFactory jmsConnectionFactory) {
		JmsTemplate template = new JmsTemplate(jmsConnectionFactory);
                // TOPIC NOT LISTEN WITH template.setPubSubDomain(true); !!!!!!!!!!!!!!!!!!
		//template.setPubSubDomain(true);
		return template;
	}

	@Bean
	public DestinationResolver destinationResolver() {
		return new DynamicDestinationResolver();
	}
    
    
    /// ========================= JMS ============================

    /*@Bean
    public Queue queue(){
        return new ActiveMQQueue("harp07qq");
    }
    
    @Bean
    public Topic topic(){
        return new ActiveMQTopic("harp07tt");
    }  
    
    @Bean 
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory amqCF=new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        //amqCF.setPassword("admin");
        //amqCF.setUserName("admin");
	return amqCF;
    }

    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> jmsListenerContainerFactory() {
	DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
	factory.setConnectionFactory(connectionFactory());
	factory.setConcurrency("3-5");
	return factory;
    }

    @Bean 
    public JmsTemplate jmsTemplate() {
	JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        // Default Destination
	jmsTemplate.setDefaultDestination(queue());
        // then use jmsTemplate.setDeliveryDelay(5000L); in ActiveMQ -> ERROR !!!!!!!
        //jmsTemplate.setDeliveryDelay(5000L);
        //jmsTemplate.setPubSubDomain(true);
	return jmsTemplate;
    }  */
    
    //========================================
    
    @Bean(name = "gson")
    public Gson gson() {
        return new Gson();
    }         

}
