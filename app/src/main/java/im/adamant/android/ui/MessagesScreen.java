package im.adamant.android.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding2.widget.RxTextView;

import im.adamant.android.AdamantApplication;
import im.adamant.android.R;
import im.adamant.android.Screens;
import im.adamant.android.helpers.LoggerHelper;
import im.adamant.android.services.SaveContactsService;
import im.adamant.android.ui.presenters.MessagesPresenter;
import im.adamant.android.ui.adapters.MessagesAdapter;
import im.adamant.android.ui.messages_support.entities.AbstractMessage;
import im.adamant.android.ui.messages_support.entities.MessageListContent;
import im.adamant.android.ui.mvp_view.MessagesView;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.BindView;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.NavigatorHolder;
import ru.terrakok.cicerone.commands.Command;
import ru.terrakok.cicerone.commands.Forward;
import ru.terrakok.cicerone.commands.SystemMessage;

public class MessagesScreen extends BaseActivity implements MessagesView {
    public static final String ARG_CHAT = "chat";

    @Inject
    NavigatorHolder navigatorHolder;

    @Inject
    Provider<MessagesPresenter> presenterProvider;

    @Inject
    MessagesAdapter adapter;

    //--Moxy
    @InjectPresenter
    MessagesPresenter presenter;

    @ProvidePresenter
    public MessagesPresenter getPresenter(){
        return presenterProvider.get();
    }

    //--ButterKnife
    @BindView(R.id.activity_messages_rv_messages) RecyclerView messagesList;
    @BindView(R.id.activity_messages_et_new_msg_text) EditText newMessageText;
    @BindView(R.id.activity_messages_btn_send) ImageButton buttonSend;
    @BindView(R.id.activity_messages_tv_cost) TextView messageCostView;
    @BindView(R.id.activity_messages_cl_empty_view) View emptyView;

    CompositeDisposable compositeDisposable = new CompositeDisposable();


    //--Activity
    @Override
    public int getLayoutId() {
        return R.layout.activity_messages_screen;
    }

    @Override
    public boolean withBackButton() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        messagesList.setLayoutManager(layoutManager);
        messagesList.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null){
            if (intent.hasExtra(ARG_CHAT)){
                String companionId = getIntent().getStringExtra(ARG_CHAT);
                presenter.onShowChatByCompanionId(companionId);
            }

            if (Intent.ACTION_VIEW.equals(intent.getAction())){
                Uri uri = intent.getData();
                if (uri != null){
                    String address = uri.getQueryParameter("address");
                    String label = uri.getQueryParameter("label");

                    presenter.onShowChatByAddress(address, label);
                }
            }
        }

        newMessageText.setOnFocusChangeListener( (view, isFocused) -> {
            if (!isFocused){
                AdamantApplication.hideKeyboard(this, newMessageText);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        navigatorHolder.setNavigator(navigator);

        Observable<String> obs = RxTextView
                .textChanges(newMessageText)
                .filter(charSequence -> charSequence.length() > 0)
                .debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map(CharSequence::toString);

        MessagesPresenter localPresenter = presenter;
        Disposable subscribe = obs.subscribe(localPresenter::onChangeMessageText);

        compositeDisposable.add(subscribe);
    }

    @Override
    protected void onPause() {
        super.onPause();
        navigatorHolder.removeNavigator();
        compositeDisposable.dispose();
        compositeDisposable.clear();
    }

    @Override
    public void showChatMessages(List<MessageListContent> messages) {
        if (messages != null){
            adapter.updateDataset(
                    messages
            );

            if (messages.size() == 0){
                emptyView.setVisibility(View.VISIBLE);
                messagesList.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                messagesList.setVisibility(View.VISIBLE);
            }

            goToLastMessage();
        }
    }

    @Override
    public void goToLastMessage() {
        messagesList.scrollToPosition(
                adapter.getItemCount() - 1
        );
    }

    @Override
    public void changeTitles(String title, String subTitle) {
        setTitle(title);
        setSubTitle(subTitle);
    }

    @Override
    public void messageWasSended(AbstractMessage message) {
        if (balanceUpdateService != null){
            balanceUpdateService.updateBalanceImmediately();
        }
        messageCostView.setText("");
    }

    @Override
    public void showMessageCost(String cost) {
        runOnUiThread( () -> messageCostView.setText(cost));
    }

    @Override
    public void showRenameDialog(String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AdamantLight_AlertDialogCustom);
        builder.setTitle(getString(R.string.dialog_rename_contact_title));

        View viewInflated = LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_rename_contact, null);

        final TextInputEditText input = viewInflated.findViewById(R.id.dialog_rename_contact_name);
        input.setText(currentName);

        builder.setView(viewInflated);

        final MessagesPresenter localPresenter = presenter;

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            if (input.getText() != null) {
                String inputText = input.getText().toString();
                localPresenter.onClickRenameButton(inputText);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void startSavingContacts() {
        Intent intent = new Intent(getApplicationContext(), SaveContactsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @OnClick(R.id.activity_messages_btn_send)
    protected void onClickSendButton() {
        presenter.onClickSendAdamantBasicMessage(
            newMessageText.getText().toString()
        );

        newMessageText.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_messages_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_rename_chat: {
                presenter.onClickShowRenameDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onClickTitle() {
        presenter.onClickShowCompanionDetail();
    }


    private Navigator navigator = new Navigator() {
        @Override
        public void applyCommands(Command[] commands) {
            for (Command command : commands){
                apply(command);
            }
        }

        private void apply(Command command){
            if(command instanceof SystemMessage){
                SystemMessage message = (SystemMessage) command;
                Toast.makeText(getApplicationContext(), message.getMessage(), Toast.LENGTH_LONG).show();
            } else if(command instanceof Forward) {
                Forward forward = ((Forward) command);
                switch (forward.getScreenKey()){
                    case Screens.LOGIN_SCREEN: {
                        Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
                        startActivity(intent);
                        MessagesScreen.this.finish();
                    }
                    break;
                    case Screens.COMPANION_DETAIL_SCREEN: {
                        Intent intent = new Intent(getApplicationContext(), CompanionDetailScreen.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(CompanionDetailScreen.ARG_COMPANION_ID, (Serializable) forward.getTransitionData());
                        intent.putExtras(bundle);

                        startActivity(intent);
                    }
                }
            }
        }
    };
}
