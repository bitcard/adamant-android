package im.adamant.android.ui.messages_support.holders;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import im.adamant.android.R;
import im.adamant.android.avatars.Avatar;
import im.adamant.android.markdown.AdamantMarkdownProcessor;
import im.adamant.android.ui.messages_support.entities.EthereumTransferMessage;
import im.adamant.android.ui.messages_support.SupportedMessageListContentType;
import im.adamant.android.ui.messages_support.entities.MessageListContent;

public class EthereumTransferMessageViewHolder extends AbstractMessageViewHolder {
    private TextView messageView;
    private TextView amountView;
    private View contentView;

    public EthereumTransferMessageViewHolder(Context context, View v, AdamantMarkdownProcessor adamantAddressProcessor, Avatar avatar) {
        super(context, v, adamantAddressProcessor, avatar);

        LayoutInflater inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.list_subitem_etherium_transfer_message, contentBlock, false);
        contentBlock.addView(contentView);

        messageView = contentView.findViewById(R.id.list_item_message_text);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        amountView = contentView.findViewById(R.id.list_item_message_amount);
    }

    @Override
    public void bind(MessageListContent message, boolean isNextMessageWithSameSender, boolean isLastMessage) {
        boolean isCorruptedMessage = (message == null) || (message.getSupportedType() != SupportedMessageListContentType.ETHEREUM_TRANSFER);

        if (isCorruptedMessage) {
            emptyView();
            return;
        }

        super.bind(message, isNextMessageWithSameSender, isLastMessage);

        EthereumTransferMessage ethereumTransferMessage = (EthereumTransferMessage) message;

        messageView.setText(
                ethereumTransferMessage.getHtmlComment(adamantAddressProcessor)
        );

        String amountText = String.format(Locale.ENGLISH, "%.8f", ethereumTransferMessage.getAmount()) + " " +
                context.getResources().getString(R.string.eth_currency_abbr);

        amountView.setText(amountText);

        displayProcessedStatus(ethereumTransferMessage);
    }
}
