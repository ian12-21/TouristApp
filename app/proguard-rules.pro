# Firestore's toObject() builds the models by reflection: it calls the no-argument
# constructor and finds properties by their getter and setter names. Without these rules R8
# renames or removes them, and every document reads back with default values.
# @get:Exclude and the generic types of the List and Map properties are read at runtime too.
-keepattributes Signature,RuntimeVisibleAnnotations,InnerClasses,EnclosingMethod
-keep class com.touristapp.data.model.** {
    <init>(...);
    <fields>;
    public *** get*();
    public *** is*();
    public void set*(***);
}

# kotlinx.serialization (the weather models) ships its own rules, so it needs none here.
