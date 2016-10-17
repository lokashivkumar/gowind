package org.gowind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.gowind.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileActivity extends AppCompatActivity {

    @BindView(R.id.addPayment) Button mAddPaymentMethodButton;
    @BindView(R.id.userName)
    TextView mUserName;
    @BindView(R.id.email) TextView mUserEmail;
    @BindView(R.id.rating) TextView mUserRating;
    @BindView(R.id.phoneNumber) TextView mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        User user = getIntent().getParcelableExtra("user");
        mUserName.setText(user.getName());
        mUserEmail.setText(user.getEmail());
        mUserRating.setText(user.getRating());
        mPhoneNumber.setText(user.getPhoneNumber());

        mAddPaymentMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
    }
}
