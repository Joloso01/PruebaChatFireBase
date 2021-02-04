package com.example.pruebafirebase;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.pruebafirebase.databinding.FragmentChatBinding;
import com.example.pruebafirebase.databinding.FragmentSignInBinding;
import com.example.pruebafirebase.databinding.ViewholderMensajeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private NavController nav;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private List<Mensaje> chat= new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return (binding = FragmentChatBinding.inflate(inflater,container,false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nav = Navigation.findNavController(view);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();

        ChatAdapter chatAdapter = new ChatAdapter();
        binding.chat.setAdapter(chatAdapter);

        binding.enviar.setOnClickListener(v -> {
            String mensaje = binding.mensaje.getText().toString();
            String fecha = LocalTime.now().toString();
            String email = mAuth.getCurrentUser().getEmail();
            String nombre = mAuth.getCurrentUser().getDisplayName();
            String foto = mAuth.getCurrentUser().getPhotoUrl().toString();

            mDB.collection("mensajes").add(new Mensaje(nombre,fecha,mensaje,email,foto));
            binding.mensaje.setText("");
        });

        binding.adjuntar.setOnClickListener(v2 -> {
            galeria.launch("image/*");
        });

        mDB.collection("mensajes").orderBy("fecha").addSnapshotListener((value, error) -> {
            chat.clear();

            value.forEach(document -> {
                chat.add(new Mensaje(
                        document.getString("nombre"),
                        document.getString("fecha"),
                        document.getString("mensaje"),
                        document.getString("email"),
                        document.getString("foto"),
                        document.getString("meme")));
            });
            chatAdapter.notifyDataSetChanged();
            binding.chat.scrollToPosition(chat.size()-1);

        });
    }

    class ChatAdapter extends RecyclerView.Adapter<MensajeViewHolder>{


        @NonNull
        @Override
        public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MensajeViewHolder(ViewholderMensajeBinding.inflate(getLayoutInflater(),parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
            Mensaje mensaje = chat.get(position);

            holder.binding.autor.setText(mensaje.nombre);
            if (mensaje.meme != null){
                Glide.with(requireView()).load(mensaje.meme).into(holder.binding.meme);
                holder.binding.mensaje.setText("");
                holder.binding.meme.setVisibility(View.VISIBLE);
                holder.binding.mensaje.setVisibility(View.GONE);
            }else {
                holder.binding.mensaje.setVisibility(View.VISIBLE);
                holder.binding.meme.setVisibility(View.GONE);
            }
            holder.binding.fecha.setText(mensaje.fecha);
            holder.binding.mensaje.setText(mensaje.mensaje);
            Glide.with(requireView()).load(mensaje.foto).into(holder.binding.foto);

            if (mensaje.nombre.equals(mAuth.getCurrentUser().getDisplayName())){
                Log.e("AGCD","a la derecha");
                holder.binding.getRoot().setGravity(Gravity.END);
            }else {
                holder.binding.getRoot().setGravity(Gravity.START);
            }
        }

        @Override
        public int getItemCount() {
            return chat.size();
        }
    }

    static  class MensajeViewHolder extends RecyclerView.ViewHolder{
        ViewholderMensajeBinding binding;
        public MensajeViewHolder(@NonNull ViewholderMensajeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri ->{
        FirebaseStorage.getInstance().getReference("imagenes/"+ UUID.randomUUID())
                .putFile(uri)
                .continueWithTask(task2 -> {
                   task2.getResult().getStorage().getDownloadUrl()
                           .addOnSuccessListener(url2 -> {
                               mDB.collection("mensajes").add(new Mensaje(mAuth.getCurrentUser().getDisplayName(),
                                       LocalTime.now().toString(),
                                       "",
                                       mAuth.getCurrentUser().getEmail(),
                                       mAuth.getCurrentUser().getPhotoUrl().toString(),
                                       url2.toString()));
                           });
                    return null;
                });
    });
}