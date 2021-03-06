package im.adamant.android.dagger.fragments;

import java.util.Map;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import im.adamant.android.Screens;
import im.adamant.android.interactors.AccountInteractor;
import im.adamant.android.interactors.chats.ChatsStorage;
import im.adamant.android.helpers.PublicKeyStorage;
import im.adamant.android.interactors.SendFundsInteractor;
import im.adamant.android.interactors.wallets.SupportedWalletFacadeType;
import im.adamant.android.interactors.wallets.WalletFacade;
import im.adamant.android.ui.messages_support.factories.MessageFactoryProvider;
import im.adamant.android.ui.presenters.SendFundsPresenter;
import io.reactivex.disposables.CompositeDisposable;
import ru.terrakok.cicerone.Router;

@Module
public class SendFundsFragmentModule {
    @FragmentScope
    @Provides
    public static SendFundsPresenter provideSendFundsPresenter(
            Router router,
            AccountInteractor accountInteractor,
            Map<SupportedWalletFacadeType, WalletFacade> wallets,
            SendFundsInteractor sendCurrencyInteractor,
            PublicKeyStorage publicKeyStorage,
            ChatsStorage chatsStorage
    ){
        return new SendFundsPresenter(
                router,
                accountInteractor,
                wallets,
                sendCurrencyInteractor,
                publicKeyStorage,
                chatsStorage
        );
    }

    @FragmentScope
    @Provides
    @Named(value = Screens.SEND_CURRENCY_TRANSFER_SCREEN)
    public static CompositeDisposable provideSendCurrencyTransferComposite() {
        return new CompositeDisposable();
    }
}
