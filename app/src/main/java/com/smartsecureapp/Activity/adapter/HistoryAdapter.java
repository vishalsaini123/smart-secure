package com.smartsecureapp.Activity.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsecureapp.Activity.callback.SmsCallback;
import com.smartsecureapp.R;

import java.io.IOException;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.CallViewHolder> {
    private MediaPlayer mediaPlayer;

    ArrayList<String> nameList;
    SmsCallback smsCallback;

    public HistoryAdapter(ArrayList<String> nameList, SmsCallback smsCallback) {
        this.nameList = nameList;
        this.smsCallback = smsCallback;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_contact_item, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        if (!nameList.get(position).isEmpty()) {
            String fullHistory = nameList.get(position);
            String[] separated = fullHistory.split("uploads/");
            String displayName = separated[1];
            holder.name.setText(displayName);

            holder.play.setOnClickListener(v -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    try {
                        mediaPlayer.setDataSource(fullHistory);
                        mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            holder.download.setOnClickListener(v -> {
                String audioName = fullHistory.split("audio/")[1];
                startDownload(holder.download.getContext(), audioName, fullHistory);
            });
        }
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class CallViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView play, download;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            play = itemView.findViewById(R.id.play);
            download = itemView.findViewById(R.id.download);
        }
    }

    private void startDownload(Context context, String name, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }
}
