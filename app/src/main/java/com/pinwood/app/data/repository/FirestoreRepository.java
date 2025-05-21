package com.pinwood.app.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Clase base para acceder a Firestore sin usar importaciones directas.
 * Usa reflexión para acceder a las clases de Firestore.
 */
public class FirestoreRepository {
    private static final String TAG = "FirestoreRepository";
    protected static final Executor executor = Executors.newSingleThreadExecutor();

    protected Object getFirestoreInstance() {
        try {
            Class<?> firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore");
            return firestoreClass.getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener instancia de Firestore: " + e.getMessage());
            return null;
        }
    }

    protected Object getCollectionReference(String collectionPath) {
        try {
            Object firestore = getFirestoreInstance();
            if (firestore != null) {
                return firestore.getClass()
                        .getMethod("collection", String.class)
                        .invoke(firestore, collectionPath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener referencia de colección: " + e.getMessage());
        }
        return null;
    }

    protected Object getDocumentReference(String collectionPath, String documentId) {
        try {
            Object collection = getCollectionReference(collectionPath);
            if (collection != null) {
                return collection.getClass()
                        .getMethod("document", String.class)
                        .invoke(collection, documentId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener referencia de documento: " + e.getMessage());
        }
        return null;
    }

    protected <T> LiveData<List<T>> getCollectionData(
            String collectionPath, 
            String whereField, 
            Object whereValue, 
            int limit,
            DocumentConverter<T> converter) {
        
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Object collectionRef = getCollectionReference(collectionPath);
                if (collectionRef == null) {
                    liveData.postValue(new ArrayList<>());
                    return;
                }

                // Aplicar filtro si es necesario
                Object query = collectionRef;
                if (whereField != null && !whereField.isEmpty()) {
                    Class<?> objectClass = Class.forName("java.lang.Object");
                    Method whereEqualToMethod = query.getClass()
                            .getMethod("whereEqualTo", String.class, objectClass);
                    query = whereEqualToMethod.invoke(query, whereField, whereValue);
                }

                // Aplicar límite si es necesario
                if (limit > 0) {
                    Method limitMethod = query.getClass().getMethod("limit", long.class);
                    query = limitMethod.invoke(query, (long) limit);
                }

                // Obtener datos
                Class<?> taskClass = Class.forName("com.google.android.gms.tasks.Task");
                Method getMethod = query.getClass().getMethod("get");
                Object task = getMethod.invoke(query);

                // Añadir listener de completado
                Class<?> completeListenerClass = Class.forName("com.google.android.gms.tasks.OnCompleteListener");
                Object completeListener = java.lang.reflect.Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class<?>[]{completeListenerClass},
                        (proxy, method, args) -> {
                            if (method.getName().equals("onComplete")) {
                                Object taskResult = args[0];
                                Boolean isSuccessful = (Boolean) taskResult.getClass()
                                        .getMethod("isSuccessful")
                                        .invoke(taskResult);

                                if (isSuccessful) {
                                    Object querySnapshot = taskResult.getClass()
                                            .getMethod("getResult")
                                            .invoke(taskResult);

                                    Method getDocumentsMethod = querySnapshot.getClass()
                                            .getMethod("getDocuments");
                                    List<?> documents = (List<?>) getDocumentsMethod.invoke(querySnapshot);

                                    List<T> resultList = new ArrayList<>();
                                    for (Object doc : documents) {
                                        Method getDataMethod = doc.getClass().getMethod("getData");
                                        Map<String, Object> data = (Map<String, Object>) getDataMethod.invoke(doc);
                                        
                                        Method getIdMethod = doc.getClass().getMethod("getId");
                                        String id = (String) getIdMethod.invoke(doc);
                                        
                                        // Añadir ID al mapa de datos si no existe
                                        if (!data.containsKey("id")) {
                                            data.put("id", id);
                                        }
                                        
                                        T item = converter.convert(data);
                                        if (item != null) {
                                            resultList.add(item);
                                        }
                                    }
                                    liveData.postValue(resultList);
                                } else {
                                    liveData.postValue(new ArrayList<>());
                                }
                            }
                            return null;
                        });

                Method addOnCompleteListenerMethod = taskClass.getMethod("addOnCompleteListener", completeListenerClass);
                addOnCompleteListenerMethod.invoke(task, completeListener);

            } catch (Exception e) {
                Log.e(TAG, "Error al obtener datos de la colección: " + e.getMessage());
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    public interface DocumentConverter<T> {
        T convert(Map<String, Object> data);
    }
}