package com.example.serino;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.serino.Fixtures.MessagesFixtures;
import com.example.serino.utils.AppUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements
        MessagesListAdapter.SelectionListener,
//        MessagesListAdapter.OnLoadMoreListener,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener{

    static final String BASE_URL = "https://10025bff.ngrok.io/";
//    static final String BASE_URL = "https://192.168.43.109:5005/";

    final String TAG = MainActivity.class.getSimpleName();
//    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader serinoImageLoader;
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;
    MessagesList messagesList;
    AVLoadingIndicatorView avi;

    private Menu menu;
    private int selectionCount;
//    private Date lastLoadedDate;

    String img_url;
    boolean isFirst = true;
    int serino_img_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serinoImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {

                if(isFirst)
                    serino_img_id = imageView.getId();
                    isFirst = false;

                if(imageView.getId()==serino_img_id)
                    Picasso.get().load(R.drawable.serino).into(imageView);
                else
                    Picasso.get().load(url).into(imageView);
            }
        };

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);

        messagesList = findViewById(R.id.messagesList);

        avi = findViewById(R.id.avi);

        initAdapter();
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);

        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                avi.show();
            }
        }, 300);

        queryResponse("0",input.toString());
        return true;
    }

    @Override
    public void onAddAttachments() {
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.get().load(R.drawable.serino).into(imageView);
            }
        };

        Message message = new Message("1",
                new User("1","Serino","https://archive.is/rdMFc/0977120b6790930d1ea77167a08785dd959ff8b5.png",true), null);
        message.setImage(new Message.Image(img_url));

        messagesAdapter.addToStart(message, true);
    }

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(senderId, serinoImageLoader);
        messagesAdapter.enableSelectionMode(this);
//        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        AppUtils.showToast(MainActivity.this,
                                message.getUser().getName() + " Avatar",
                                false);
                    }
                });
        messagesList.setAdapter(messagesAdapter);
    }

    @Override
    public void onStartTyping() {
        Log.v("Typing listener", getString(R.string.start_typing_status));
    }

    @Override
    public void onStopTyping() {
        Log.v("Typing listener", getString(R.string.stop_typing_status));
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                messagesAdapter.addToStart(
                        new Message("1",
                                new User("1","Serino","https://archive.is/rdMFc/0977120b6790930d1ea77167a08785dd959ff8b5.png",true),
                                "Habari, Naitwa Serino"),
                        true);
            }
        }, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, R.string.copied_message, true);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

//    @Override
//    public void onLoadMore(int page, int totalItemsCount) {
//        Log.i("TAG", "onLoadMore: " + page + " " + totalItemsCount);
//        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
//            loadMessages();
//        }
//    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

//    protected void loadMessages() {
//        new Handler().postDelayed(new Runnable() { //imitation of internet connection
//            @Override
//            public void run() {
//                ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
//                lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
//                messagesAdapter.addToEnd(messages, false);
//            }
//        }, 1000);
//    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    private void queryResponse(String sender, String message){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

//        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        QueryApi queryApi = retrofit.create(QueryApi.class);

        Call<List<QueryResponse>> call= queryApi.queryResponse(new Query(sender,message));

        call.enqueue(new Callback<List<QueryResponse>>() {
            @Override
            public void onResponse(Call<List<QueryResponse>> call, Response<List<QueryResponse>> response) {
                if(response.isSuccessful()){
                    List<QueryResponse> postList = response.body();
                    Log.d(TAG, "Returned count " + postList.size());

                    avi.hide();
                    for(QueryResponse queryResponse: postList){
                        if(queryResponse.getText()!=null){
                            messagesAdapter.addToStart(
                                    new Message("1",
                                            new User("1","Serino","Nil",true),
                                            queryResponse.getText()),
                                    true);
                        }else{
                            img_url = queryResponse.getImage();
                            onAddAttachments();
                        }
                    }
                }else {
                    avi.hide();
                }
            }

            @Override
            public void onFailure(Call<List<QueryResponse>> call, Throwable t) {
                //showErrorMessage();
                avi.hide();
                Log.d(TAG, "error loading from API");
                Log.d(TAG, t.getLocalizedMessage());
            }
        });
    }
}
