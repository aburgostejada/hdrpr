package ahxsoft.hdrpr;

public interface CallableReturn<V, P> {
    V call(P param) throws Exception;
}


