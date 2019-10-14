package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //Arraylist that contains all strings to display on listview
    private ArrayList<String> arrayList;
    //Arraylist that contains all song objects
    ArrayList<Songs> holdSongs = new ArrayList<>();
    private static final int MY_PERMISSION_REQUEST = 1;
    ListView listView;
    ArrayAdapter<String> adapter;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler seekHandler;
    private SeekBar sb;

    //MUST FIX RESUME
    private int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        seekHandler = new Handler();
        setContentView(R.layout.activity_main);

        Button bw = findViewById(R.id.rewind);
        //When rewind button is clicked, call rewind method
        bw.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rewind();
            }
        });

        Button ff = findViewById(R.id.forwards);
        //When forward button is clicked, call fast forward method
        ff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                forward();
            }
        });

        ImageButton playPause = findViewById(R.id.play);
        //Sets the image of button to play
        playPause.setImageResource(R.drawable.play);
        //When play button pressed, call the playButtonPauseButton method
        //Method will determine whether it is play or pause
        playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playButtonPausePressed();
            }
        });

        sb = findViewById(R.id.seekBar);
        //Initialize the seekbar to beginning
        sb.setProgress(0);
        //Used to keep track if the seekbar is change
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //Used to move the song to a specific position
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ImageView coverArt = findViewById(R.id.coverArt);
        coverArt.setImageResource(R.drawable.blankalbumart);

        //Used to request permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            playSong();
        }
    }

    //Used to retrieve cover art from a path and set it
    public void setCoverArt(String path) {
        ImageView coverArt = findViewById(R.id.coverArt);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        //Get data from path
        mmr.setDataSource(path);
        //Retrieve picture from mmr
        byte[] data = mmr.getEmbeddedPicture();

        //If there is a picture
        if (data != null) {
            //Set the picture from retrieved path
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            coverArt.setImageBitmap(bm);
        }
        else {
            //If there is no album art, set blank album art
            coverArt.setImageResource(R.drawable.blankalbumart);
        }
    }

    //Set the song title in music player
    public void setSongName(int position) {
        TextView songName = findViewById(R.id.songName);
        //Set the song name retrieved to the next view
        songName.setText(holdSongs.get(position).getTitle());
    }

    //Play song from a specified uri
    public void playNewSong(Uri uri) {
        //If there is a song playing, stop the song
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            //Stop the song
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        //Plays the song from specified uri
        mediaPlayer = mediaPlayer.create(this, uri);
        mediaPlayer.start();
    }

    //Fast forward
    public void forward() {
        //Retrieve current position of song playing
        int pos = mediaPlayer.getCurrentPosition();
        //If no song playing, get out of the method
        if (mediaPlayer == null) {
            return;
        }
        //Add 15000 milliseconds to current position
        pos += 15000;
        //Move to the position
        mediaPlayer.seekTo(pos);
        //Update seek bar
        setSeekBar();
    }

    //Rewind song
    public void rewind() {
        //Retrieve current position of song playing
        int pos = mediaPlayer.getCurrentPosition();
        //If there is no song playing, get out of the method
        if (mediaPlayer == null) {
            return;
        }
        //Subtract 15000 milliseconds from current position
        pos -= 15000;
        //Move to the position
        mediaPlayer.seekTo(pos);
        //Update seek bar
        setSeekBar();
    }

    //Used to update seek bar
    public void setSeekBar() {
        sb = findViewById(R.id.seekBar);
        //Used to set the max of the seek bar which is how long the song is
        sb.setMax(mediaPlayer.getDuration());
        //Get the current position of seek bar
        sb.setProgress(mediaPlayer.getCurrentPosition());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setSeekBar();
            }
        };
        //Used to update seek bar every 2 milliseconds
        seekHandler.postDelayed(runnable, 2);
    }

    //Contains Bug where pause goes back to zero
    public void playButtonPausePressed() {
        TextView songName = findViewById(R.id.songName);
        ImageButton pp = findViewById(R.id.play);
        //Used to play random song when pressed play and no music is playing
        if (!mediaPlayer.isPlaying() && songName.getText() != "No Song Playing") {
            //Used to generate random number
            int randomNum = (int) Math.random() * holdSongs.size();
            //If there are songs
            if (randomNum != 0) {
                //Retrieve song information from that index
                Songs play = holdSongs.get(randomNum);
                //Set the song name
                setSongName(randomNum);
                //Convert the path to URI
                Uri myUri = Uri.parse(play.getPath());
                //Play the song at the URI
                playNewSong(myUri);
                //Update cover art of the path
                setCoverArt(play.getPath());
                //Update to display pause button
                pp.setImageResource(R.drawable.pause);
                try {
                    mediaPlayer.setDataSource(MainActivity.this, myUri);
                    mediaPlayer.prepare();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    setSeekBar();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //Used to control the pause button
        else if (mediaPlayer.isPlaying()) {
            time = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            pp.setImageResource(R.drawable.play);
        }
        //Used to resume song playing
        else {
            mediaPlayer.seekTo(time);
            mediaPlayer.start();
        }
    }

    //Used to retrieve all songs from phone
    public void getSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null,
                null, null, null);

        //If phone contains songs
        if (songCursor != null && songCursor.moveToFirst()) {
            do {
                //Extract the data from phone
                String currentTitle = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String currentArtist = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String currentPath = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                //Store data into data structure that  used to display list view
                arrayList.add("Title: " + currentTitle + "\n" + "Artist: " + currentArtist);

                //Used to create Song objects
                Songs newObj = new Songs();
                newObj.setTitle(currentTitle);
                newObj.setArtist(currentArtist);
                newObj.setPath(currentPath);
                setSeekBar();
                holdSongs.add(newObj);
            }
            //Keep running loops where there are songs
            while (songCursor.moveToNext());
        }
    }

    //Used to play song when a certain song on a list is selected
    public void playSong() {
        //Contains list of all songs
        listView = findViewById(R.id.view);
        arrayList = new ArrayList<>();
        getSongs();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        //Used to display the list
        listView.setAdapter(adapter);
        if (!mediaPlayer.isPlaying()) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageButton pp = findViewById(R.id.play);
                    //Get the position of the list clicked
                    Songs newSongs = holdSongs.get(position);
                    //Update seek bar
                    setSeekBar();
                    //Update song name
                    setSongName(position);
                    //Get the path of the song
                    Uri myUri = Uri.parse(newSongs.getPath());
                    playNewSong(myUri);
                    //Update ocver art
                    setCoverArt(newSongs.getPath());
                    //Update button to pause
                    pp.setImageResource(R.drawable.pause);
                    try {
                        mediaPlayer.setDataSource(MainActivity.this, myUri);
                        mediaPlayer.prepare();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            //Stop current song playing
            mediaPlayer.stop();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Get information of position of list clicked
                    Songs newSongs = holdSongs.get(position);
                    //Update seek bar
                    setSeekBar();
                    //Update the song name
                    setSongName(position);
                    //Convert path to URI
                    Uri myUri = Uri.parse(newSongs.getPath());
                    //Play song at current URI
                    playNewSong(myUri);
                    //Update cover art
                    setCoverArt(newSongs.getPath());
                    ImageButton pp = findViewById(R.id.play);
                    //Update the button to pause
                    pp.setImageResource(R.drawable.pause);
                    try {
                        mediaPlayer.setDataSource(MainActivity.this, myUri);
                        mediaPlayer.prepare();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}