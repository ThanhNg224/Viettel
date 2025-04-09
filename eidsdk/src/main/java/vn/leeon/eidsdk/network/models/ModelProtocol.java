package vn.leeon.eidsdk.network.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ModelProtocol {

    @NonNull
    default String searchCriteria() {
        return "";
    }

    @Nullable
    default String statusMessage() {
        return "";
    }

    boolean isValidModel();
}
