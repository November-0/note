package com.example.memorandum.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memorandum.R;
import com.example.memorandum.activity.MemorandumActivity;
import com.example.memorandum.bean.Data;
import com.example.memorandum.bean.User;
import com.example.memorandum.dao.DataDAO;
import com.example.memorandum.util.AppGlobal;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Boogie on 2021/06/04.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private Context context;
    private final List<Data> mDataList;
    private Button favorite;
    public static final int favoriteNo = 1;
    public static final int favoriteYes = 2;
    DataDAO dataDAO = new DataDAO();

    /**
     *在主页创建之后会查询数据库中适合的备忘，并以此构造Adapter。同时还需要实现点击元素的行为代码。
     * 在此使用handler异步接收并处理信息，根据信息的不同调用不同的功能（更换ui），这么做的目的是防止改变UI时页面的闪烁。
     * */
    private static final String TAG = "DataAdapter";
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(@NonNull Message message) {
            //handler接收并处理消息，根据不同case决定收藏button的样式和文字
            switch (message.what) {
                case favoriteNo:
                    favorite.setText("Like this !!");
                    favorite.setBackgroundResource(R.drawable.btn_mark);
                    break;
                case favoriteYes:
                    favorite.setText("No...");
                    favorite.setBackgroundResource(R.drawable.btn_mark_grey);
                    break;
                default:
                    favorite.setText("Like this !!");
                    favorite.setBackgroundResource(R.drawable.btn_mark);
                    break;
            }
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        View dataView;
        TextView dataDate;
        TextView dataContent;
        ImageView dataPending;
        ImageView dataReminder;
        ImageView dataStar;
        Intent intent = new Intent();

        public ViewHolder(View view) {
            super(view);
            dataView = view;
            dataDate = (TextView) view.findViewById(R.id.data_date);
            dataContent = (TextView) view.findViewById(R.id.data_content);
            dataPending = (ImageView) view.findViewById(R.id.data_pending);
            dataReminder = (ImageView) view.findViewById(R.id.data_reminder);
            dataStar = (ImageView) view.findViewById(R.id.data_star);
            View main = itemView.findViewById(R.id.main);
            main.setOnClickListener(this);
            main.setOnLongClickListener(this);
            Button delete = (Button) itemView.findViewById(R.id.delete);
            favorite = (Button) itemView.findViewById(R.id.favorite);
            delete.setOnClickListener(this);
            favorite.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            /**
             *主页点击对应的备忘之后会跳转到对应的查看、编辑界面，这个过程先需要获取点击的位置，
             * 然后获取对应的Data，关键信息是id、content、date等数据，之后将这些数据都put到intent中，跳转到新的界面。
             * */
            int position = getAdapterPosition();
            //根据用户点击获取当前位置
            Data data = mDataList.get(position);
            int cur_user_id = 0;
            User cur_user = data.getUser();
            if (cur_user != null) {
                cur_user_id = cur_user.getId();
            } else {
                cur_user_id = -1;
            }
            Log.d("ID", String.valueOf(cur_user_id));
            //将当前位置传入对应的data对象
            int id = data.getId();
            favorite = (Button) itemView.findViewById(R.id.favorite);
            switch (v.getId()) {
                case R.id.main:
                    String date = data.getDate();
                    String content = data.getContent();
                    String imagePath = data.getImagePath();

                    intent.setClass(v.getContext(), MemorandumActivity.class);
                    intent.putExtra("id", id);
                    //向intent中传入当前对象的属性的值
                    intent.putExtra("content", content);
                    intent.putExtra("date", date);
                    intent.putExtra("data", data);
                    if (imagePath != null && !imagePath.equals("")) {
                        intent.putExtra("imagePath", imagePath);
                    }
                    v.getContext().startActivity(intent);
                    break;

                    /**
                     * 对于显示在主页的每一项都设置了一个左滑动作，可以对备忘进行收藏和删除。
                     * 这个UI是依靠“liyi”开源的SwipeItemLayout框架实现的，
                     * 使用的时候需要将他指定给recyclerView的addOnItemTouchListener方式以使用。
                     * 还需要在对应item的adapter的布局文件中写入，以及在对应Java代码中实现点击的行为。
                     * 而在这里，未登录用户是不能改变某一特定用户的备忘的收藏状态的，
                     * 也不能删除某一特定用户创建的备忘。但如果备忘本来就是由未登录用户创建的，
                     * 未登录用户就可以收藏和删除。
                     * 点击“收藏”之后，除了更新数据库外，还会使用handler发送消息异步改变UI。
                     * */
                case R.id.favorite: {
                    int chooseStar = data.getStar();
                    Message message = new Message();

                    if (AppGlobal.USERNAME.equals("")) {
                        if (cur_user_id == -1) {
                            // Toast.makeText(v.getContext(), "Of course...", Toast.LENGTH_SHORT).show();
                        } else {
                            //非本人操作
                            Toast.makeText(v.getContext(), "Go away !", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (chooseStar == 2) {
                        //取消收藏
                        data.setStar(1);
                        data.update(id);
                        chooseStar = data.getStar();
                        Log.d(TAG, String.valueOf(chooseStar));
                        favorite.setText("Like it !!");
                        favorite.setBackgroundResource(R.drawable.btn_mark);
                        Toast.makeText(v.getContext(), "No...", Toast.LENGTH_SHORT).show();
                        message.what = favoriteNo;
                        //发送favoriteNo的消息
                    } else {
                        //收藏
                        data.setStar(2);
                        data.update(id);
                        chooseStar = data.getStar();
                        Log.d(TAG, String.valueOf(chooseStar));
                        favorite.setText("No...");
                        favorite.setBackgroundResource(R.drawable.btn_mark_grey);
                        Toast.makeText(v.getContext(), "Like it !!", Toast.LENGTH_SHORT).show();
                        message.what = favoriteYes;
                        //发送favoriteYes的消息
                    }
                    handler.sendMessage(message);
                    //使用handler发送消息
                    break;
                }

                case R.id.delete:
                    if (AppGlobal.USERNAME.equals("")) {
                        if (cur_user_id == -1) {
                            // Toast.makeText(v.getContext(), "Of course...", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(), "Do not do bad things !", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    DataSupport.delete(Data.class, id);
                    mDataList.remove(position);
                    notifyItemRemoved(position);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public DataAdapter(List<Data> dataList) {
        mDataList = dataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.data_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = mDataList.get(position);
        int currentId = data.getId();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");//转换时间格式
        Date date = new Date(System.currentTimeMillis());
        try {
            /*
            直接显示全部的年月日时间
            holder.dataDate.setText(data.getDate());
            */
            if (!simpleDateFormat.format(date).equals(data.getDate().substring(0, 11))) {
                //如果前11位不相同说明不是同一天，只显示年月日
                holder.dataDate.setText(data.getDate().substring(5, 11));
            } else {
                //如果前11位相同说明是同一天，只显示具体的时间
                holder.dataDate.setText(data.getDate().substring(12));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data.getContent() != null) {
            //限制长度
            if (data.getContent().length() <= 60) {
                holder.dataContent.setText(data.getContent());
            } else {
                holder.dataContent.setText(data.getContent().substring(0, 60) + "...");
            }
        }
        showPending(holder, data.getPending());
        showReminder(holder, data.getReminder());
        showStar(holder, data.getStar());
        for (int i = 0; i <= position; i++) {
            if (data.getStar() == 2) {
                favorite.setText("No...");
                favorite.setBackgroundResource(R.drawable.btn_mark_grey);


            } else if (data.getStar() == 1) {
                favorite.setText("Like it !!");
                favorite.setBackgroundResource(R.drawable.btn_mark);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private void showPending(ViewHolder holder, int currentPending) {
        if (currentPending == 2) {
            Glide.with(context).load(R.drawable.pending).asBitmap().into(holder.dataPending);
            //使用Glide向imageview中加载图片
        } else {
//            Glide.with(context).load(R.drawable.pending_plain).asBitmap().into(holder.dataPending);
//           holder.dataPending.setBackgroundResource(0);
            holder.dataPending.setWillNotDraw(true);
            //利用holder找到当前视图位置，设置图片绘制为不绘制
        }
    }

    private void showReminder(ViewHolder holder, int currentReminder) {
        if (currentReminder == 2) {
            Glide.with(context).load(R.drawable.reminder).asBitmap().into(holder.dataReminder);
        } else {
//            Glide.with(context).load(R.drawable.reminder_plain).asBitmap().into(holder.dataReminder);
//            holder.dataReminder.setBackgroundResource(0);
            holder.dataReminder.setWillNotDraw(true);
        }
    }

    /**
     * 备忘的右下角会显示收藏状态、备忘状态，在这里使用使用Glide向imageview中加载图片。
     * */
    private void showStar(ViewHolder holder, int currentStar) {

        if (currentStar == 2) {
            Glide.with(context).load(R.drawable.star).asBitmap().into(holder.dataStar);
        } else {
//            Glide.with(context).load(R.drawable.star_plain).asBitmap().into(holder.dataStar);
//            holder.dataStar.setBackgroundResource(0);
            holder.dataStar.setWillNotDraw(true);
        }
    }

}
