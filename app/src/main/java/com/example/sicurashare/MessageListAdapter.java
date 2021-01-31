package com.example.sicurashare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<MessageObject> messageList;

    public MessageListAdapter(ArrayList<MessageObject> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;

    }
    public interface OnSingleItemClickListener {
        void onSingleItemClick(int position, String message);

        void onSingleLongItemClick(int position);
    }

    private OnSingleItemClickListener mlistener;

    public void setSingletOnItemClickListener(OnSingleItemClickListener listener) {
        mlistener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getStatus().equals("received")) {
            return 0;
        }
        return 1;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == 0) {
            view = layoutInflater.inflate(R.layout.message_item_receive_layout, parent, false);
            return new messageListViewHolderReceive(view, mlistener);
        }
        view = layoutInflater.inflate(R.layout.message_item_send_layout, parent, false);
        return new messageListViewHolderSend(view, mlistener);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (messageList.get(position).getStatus().equals("received"))
        {
            final messageListViewHolderReceive messageListViewHolderReceive = (MessageListAdapter.messageListViewHolderReceive) holder;
            messageListViewHolderReceive.message.setText(messageList.get(position).getMsg());
            messageListViewHolderReceive.time.setText(messageList.get(position).getTime());
        }
        else
        {
            final messageListViewHolderSend messageListViewHolderSend = (MessageListAdapter.messageListViewHolderSend) holder;
            messageListViewHolderSend.messageSend.setText(messageList.get(position).getMsg());
            messageListViewHolderSend.timeSend.setText(messageList.get(position).getTime());

        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class messageListViewHolderReceive extends RecyclerView.ViewHolder {


        TextView message, time;

        messageListViewHolderReceive(@NonNull final View itemView, final OnSingleItemClickListener listener) {
            super(itemView);

            message = itemView.findViewById(R.id.msgBox);
            time = itemView.findViewById(R.id.timeBox);




            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleItemClick(position, "receiver");

                        }
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleLongItemClick(position);
                        }
                    }

                    return true;
                }
            });


        }


    }

    class messageListViewHolderSend extends RecyclerView.ViewHolder {


        TextView messageSend, timeSend;


        public messageListViewHolderSend(@NonNull final View itemView, final OnSingleItemClickListener listener) {
            super(itemView);

            messageSend = itemView.findViewById(R.id.msgBoxSend);
            timeSend = itemView.findViewById(R.id.timeBoxSend);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleItemClick(position, "sender");


                        }
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleLongItemClick(position);
                        }
                    }

                    return true;
                }
            });


        }


    }
}
