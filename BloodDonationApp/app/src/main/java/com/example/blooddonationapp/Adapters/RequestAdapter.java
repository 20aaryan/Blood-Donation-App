package com.example.blooddonationapp.Adapters;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blooddonationapp.Activities.DonorRegistrationFormActivity;
import com.example.blooddonationapp.Activities.RegisteredMsg;
import com.example.blooddonationapp.MainActivityAdmin;
import com.example.blooddonationapp.ModelClasses.Donor;
import com.example.blooddonationapp.ModelClasses.Notification;
import com.example.blooddonationapp.ModelClasses.Patient;
import com.example.blooddonationapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{

    private EditText donorName,donorPhone,donorAge,donorEmail,blood_group,donorLocation,donorPdfUri;
    private View bloodList;
    private ImageView drop_up;
    private Button register;
    private CheckBox isPublicBox;
    private Boolean isValid=false;
    private Boolean isPublic=false;
    public String isAdmin="false";
    private DatabaseReference mDatabase;

    Context context;
    ArrayList<Patient> patientArrayList;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseFirestore db,db1,db2,db3;
    public RequestAdapter(Context context, ArrayList<Patient> patientArrayList) {
        this.context = context;
        this.patientArrayList = patientArrayList;
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(context).inflate(R.layout.item_request,parent,false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        Patient patient = patientArrayList.get(position);
        holder.name.setText(patient.getUserName());
        holder.bloodGrp.setText(patient.getBloodGrp());
        holder.location.setText(patient.getLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialog();
                final Dialog dialog;
                dialog = new Dialog(holder.itemView.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.bottom_sheet_request_detail);

                //Loading details from firebase firestore
                TextView userName,patientName,bloodGrp,units,age,location,message,msg;
                Button donate;
                ImageView share;
                share = dialog.findViewById(R.id.share);

                userName=dialog.findViewById(R.id.name);
                patientName=dialog.findViewById(R.id.patient_name);
                bloodGrp=dialog.findViewById(R.id.blood_group);
                units=dialog.findViewById(R.id.units);
                age=dialog.findViewById(R.id.age);
                location=dialog.findViewById(R.id.location);
                donate=dialog.findViewById(R.id.donate);
                message=dialog.findViewById(R.id.message);
                msg=dialog.findViewById(R.id.msg);

                userName.setText(patient.getUserName());
                patientName.setText(patient.getPatientName());
                bloodGrp.setText(patient.getBloodGrp());
                units.setText(patient.getRequiredUnits());
                age.setText(patient.getAge());
                location.setText(patient.getLocation());
                if(patient.getAdditionalDetails().length()>0)
                {
                    msg.setVisibility(View.VISIBLE);
                }
                message.setText(patient.getAdditionalDetails());

                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);

                        String msg = patient.getPatientName() +" requires " +patient.getRequiredUnits()
                                +" units of " +patient.getBloodGrp() + " blood at "
                                + patient.getLocation();

                        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                        sendIntent.setType("text/plain");

                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        dialog.getContext().startActivity(shareIntent);
                    }
                });

                db = FirebaseFirestore.getInstance();
                db.collection("Admin").document(currentUser.getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists())
                        {
                            String signedin=task.getResult().getString("Signed_in");
                            if(!signedin.equals("true"))
                            {
                                donate.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

                donate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mAuth=FirebaseAuth.getInstance();
                        currentUser=mAuth.getCurrentUser();
                        db = FirebaseFirestore.getInstance();
                        db1 = FirebaseFirestore.getInstance();
                        db2 = FirebaseFirestore.getInstance();
                        db3 = FirebaseFirestore.getInstance();
                        db.collection("Registered Donors").document(currentUser.getPhoneNumber())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.getResult().exists())
                                {
                                    Map<String,Object> m=new HashMap<>();
                                    m.put("Donor Number",currentUser.getPhoneNumber());
                                    m.put("Patient Number",patient.getUserPhone());
                                    //Request Send
                                    Toast.makeText(holder.itemView.getContext(), "Request Sent",Toast.LENGTH_LONG).show();

                                    db.collection("Users").document(patient.getUserName()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            String seekerEmail=task.getResult().getString("email");
                                            //Email
                                            //To seeker i.e. on seekerEmail that a donor has asked for blood donation
                                            //and donor's contact details


                                            //Get donor's detail from here
                                            db.collection("Registered Donors").document(patient.getUserPhone()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    //example
                                                    String name=task.getResult().getString("name");
                                                    String contact=task.getResult().getString("phone");;
                                                    String location=task.getResult().getString("location");;
                                                    String email=task.getResult().getString("email");

                                                    //Notifying
                                                    mDatabase = FirebaseDatabase.getInstance().getReference();
                                                    //Notifying
                                                    com.example.blooddonationapp.ModelClasses.Notification notification=new Notification();
                                                    notification.setLine1("Donor Found!!");
                                                    notification.setLine2(name+" has asked to donate");
                                                    notification.setLine3("Contact details"+contact+" "+email+" "+location);
                                                    notification.setSeen(false);

                                                    mDatabase.child("Notifications")
                                                            .child(patient.getUserPhone()).push().setValue(notification);
                                                    //Email


                                                }
                                            });


                                        }
                                    });

                                    db3.collection("Blood Donation Request").
                                            document(currentUser.getPhoneNumber()+"-"+patient.getUserPhone())
                                            .set(m);


                                }
                                else
                                {
                                    db2.collection("Donor Requests").document(currentUser.getPhoneNumber())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()) {
                                                Toast.makeText(holder.itemView.getContext(),
                                                        "Your request has been registered, we will contact you soon " +
                                                                "after manual verification of documents", Toast.LENGTH_LONG).show();

                                            } else {
                                                //Directly Register
                                                dialog.cancel();
                                                Intent i = new Intent(holder.itemView.getContext(), DonorRegistrationFormActivity.class);
                                                context.startActivity(i);
                                            }
                                        }
                                     });


                                }
                            }
                        });


                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            }
        });

    }

    @Override
    public int getItemCount() {
        return patientArrayList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView name,bloodGrp,location;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.request_list_name);
            bloodGrp=itemView.findViewById(R.id.request_list_blood_grp);
            location=itemView.findViewById(R.id.request_list_location);
        }
    }




//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if(requestCode==12 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
////        {
////            documents.setText(data.getDataString());
////            uri=data.getData();
////        }
//   }



}
