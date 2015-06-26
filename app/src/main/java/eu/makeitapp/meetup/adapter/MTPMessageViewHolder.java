package eu.makeitapp.meetup.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import eu.makeitapp.meetup.R;


/**
 * ${PROJECT}
 * <p/>
 * Created by Federico Oldrini (federico.oldrini@makeitapp.eu) on 03/04/2015.
 */
public class MTPMessageViewHolder extends RecyclerView.ViewHolder {

    public TextView messageTextView;
    public TextView senderTextView;
    public ImageView attachmentImageView;
    public CardView cardView;

    public MTPMessageViewHolder(View itemView) {
        super(itemView);
        messageTextView = (TextView) itemView.findViewById(R.id.tv__message);
        senderTextView = (TextView) itemView.findViewById(R.id.tv__sender);
        attachmentImageView = (ImageView) itemView.findViewById(R.id.iv__attachment);
        cardView = (CardView) itemView.findViewById(R.id.card_view);
    }
}
