package org.loed.framework.common.web.validate;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.beans.factory.FactoryBean;

import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 * 验证器工厂构建器
 *
 * @author Thomason
 * @version 1.0
 * @since 11-11-22 下午11:47
 */

public class ValidatorFactoryBean implements FactoryBean<ValidatorFactory> {
	private MessageInterpolator messageProvider;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * <p>As with a {@link org.springframework.beans.factory.BeanFactory}, this allows support for both the
	 * Singleton and Prototype design pattern.
	 * <p>If this FactoryBean is not fully initialized yet at the time of
	 * the call (for example because it is involved in a circular reference),
	 * throw a corresponding {@link org.springframework.beans.factory.FactoryBeanNotInitializedException}.
	 * <p>As of Spring 2.0, FactoryBeans are allowed to return <code>null</code>
	 * objects. The factory will consider this as normal value to be used; it
	 * will not throw a FactoryBeanNotInitializedException in this case anymore.
	 * FactoryBean implementations are encouraged to throw
	 * FactoryBeanNotInitializedException themselves now, as appropriate.
	 *
	 * @return an instance of the bean (can be <code>null</code>)
	 * @throws Exception in case of creation errors
	 * @see org.springframework.beans.factory.FactoryBeanNotInitializedException
	 */
	@Override
	public ValidatorFactory getObject() throws Exception {
		HibernateValidatorConfiguration configure = Validation.byProvider(HibernateValidator.class).configure();
		configure.messageInterpolator(messageProvider);
		return configure.buildValidatorFactory();
	}

	/**
	 * Return the type of object that this FactoryBean creates,
	 * or <code>null</code> if not known in advance.
	 * <p>This allows one to check for specific types of beans without
	 * instantiating objects, for example on autowiring.
	 * <p>In the case of implementations that are creating a singleton object,
	 * this method should try to avoid singleton creation as far as possible;
	 * it should rather estimate the type in advance.
	 * For prototypes, returning a meaningful type here is advisable too.
	 * <p>This method can be called <i>before</i> this FactoryBean has
	 * been fully initialized. It must not rely on state created during
	 * initialization; of course, it can still use such state if available.
	 * <p><b>NOTE:</b> Autowiring will simply ignore FactoryBeans that return
	 * <code>null</code> here. Therefore it is highly recommended to implement
	 * this method properly, using the current state of the FactoryBean.
	 *
	 * @return the type of object that this FactoryBean creates,
	 * or <code>null</code> if not known at the time of the call
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType
	 */
	@Override
	public Class<?> getObjectType() {
		return ValidatorFactory.class;
	}

	/**
	 * Is the object managed by this factory a singleton? That is,
	 * will {@link #getObject()} always return the same object
	 * (a reference that can be cached)?
	 * <p><b>NOTE:</b> If a FactoryBean indicates to hold a singleton object,
	 * the object returned from <code>getObject()</code> might get cached
	 * by the owning BeanFactory. Hence, do not return <code>true</code>
	 * unless the FactoryBean always exposes the same reference.
	 * <p>The singleton status of the FactoryBean itself will generally
	 * be provided by the owning BeanFactory; usually, it has to be
	 * defined as singleton there.
	 * <p><b>NOTE:</b> This method returning <code>false</code> does not
	 * necessarily indicate that returned objects are independent instances.
	 * An implementation of the extended {@link org.springframework.beans.factory.SmartFactoryBean} interface
	 * may explicitly indicate independent instances through its
	 * {@link org.springframework.beans.factory.SmartFactoryBean#isPrototype()} method. Plain {@link org.springframework.beans.factory.FactoryBean}
	 * implementations which do not implement this extended interface are
	 * simply assumed to always return independent instances if the
	 * <code>isSingleton()</code> implementation returns <code>false</code>.
	 *
	 * @return whether the exposed object is a singleton
	 * @see #getObject()
	 * @see org.springframework.beans.factory.SmartFactoryBean#isPrototype()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setMessageProvider(MessageInterpolator messageProvider) {
		this.messageProvider = messageProvider;
	}
}
