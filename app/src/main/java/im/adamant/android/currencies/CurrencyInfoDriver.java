package im.adamant.android.currencies;

import java.math.BigDecimal;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.internal.operators.single.SingleHide;

public interface CurrencyInfoDriver {
    BigDecimal getBalance();
    String getAddress();
    SupportedCurrencyType getCurrencyType();
    //TODO: Remove hardcoded values
    String getTitle();
    int getPrecision();
    int getBackgroundLogoResource();
    Single<List<CurrencyTransferEntity>> getLastTransfers();
}
