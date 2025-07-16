package mg.itu.prom.build;

public interface BeanInstantiator<T> {
    T instantiate() throws Exception;
}
