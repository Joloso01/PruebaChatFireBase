package com.example.pruebafirebase;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.pruebafirebase.databinding.FragmentSignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firestore.v1.UpdateDocumentRequest;

import java.util.UUID;

public class SignUpFragment extends Fragment {

    public static class SignUpViewModel extends ViewModel{
        Uri imagen;
    }

    private SignUpViewModel vm;
    private FragmentSignUpBinding binding;
    private NavController nav;
    private FirebaseAuth mAuth;
    private Uri imagenSeleccionada;
    private FirebaseStorage firebaseStorage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return (binding = FragmentSignUpBinding.inflate(inflater,container,false)).getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nav = Navigation.findNavController(view);
        mAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        vm = new ViewModelProvider(this).get(SignUpViewModel.class);

        binding.emailSignUp.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.paswword.getText().toString();
            String name = binding.name.getText().toString();

           mAuth.createUserWithEmailAndPassword(email,password)
                   .addOnCompleteListener(task -> {
               if (task.isSuccessful()){
                   firebaseStorage.getReference("avatars/"+ UUID.randomUUID())
                   .putFile(vm.imagen)
                   .continueWithTask(task1 -> {
                               task1.getResult().getStorage().getDownloadUrl()
                                       .addOnSuccessListener(url -> {

                                           mAuth.getCurrentUser()
                                                   .updateProfile(
                                                           new UserProfileChangeRequest
                                                                   .Builder()
                                                                   .setDisplayName(name)
                                                                   .setPhotoUri(url)
                                                                   .build());
                                       });
                       return null;
                   });

                   nav.navigate(R.id.action_signUpFragment_to_chatFragment);
               }else{
                   Toast.makeText(getContext(), task.getException().getLocalizedMessage(),Toast.LENGTH_LONG).show();
               }
           });

        });

        binding.foto.setOnClickListener(v -> {
            galeria.launch("image/*");
        });
        if (vm.imagen != null){
            Glide.with(requireView()).load(vm.imagen).into(binding.foto);
        }

    }

    ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri ->{
        Glide.with(requireView()).load(uri).into(binding.foto);
        vm.imagen=uri;
    });
}