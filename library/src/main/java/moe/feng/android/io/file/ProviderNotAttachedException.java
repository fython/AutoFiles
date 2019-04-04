package moe.feng.android.io.file;

public class ProviderNotAttachedException extends RuntimeException {

    public ProviderNotAttachedException(int providerId) {
        super("Provider (id=" + providerId + ") haven\'t attached.");
    }

}
