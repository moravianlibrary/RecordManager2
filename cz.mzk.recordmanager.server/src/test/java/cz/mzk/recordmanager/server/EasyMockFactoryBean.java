package cz.mzk.recordmanager.server;

import org.springframework.beans.factory.FactoryBean;

import org.easymock.EasyMock;

public class EasyMockFactoryBean<T> implements FactoryBean<T> {

    private Class<T> mockedClass;

	public void setMockedClass(Class<T> mockedClass) {
        this.mockedClass = mockedClass;
    } 

    public T getObject() throws Exception {
        return EasyMock.createStrictMock(mockedClass);
    }

    public Class<T> getObjectType() {
        return mockedClass;
    }

    public boolean isSingleton() {
        return true;
    } 

}
