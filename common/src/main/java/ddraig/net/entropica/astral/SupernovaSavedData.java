package ddraig.net.entropica.astral;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public class SupernovaSavedData extends SavedData {

    private final List<SupernovaEvent> events = new ArrayList<>();

    public static final Codec<SupernovaSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SupernovaEvent.CODEC.listOf().fieldOf("events").forGetter(SupernovaSavedData::getEvents)
    ).apply(instance, SupernovaSavedData::new));

    public static final SavedDataType<SupernovaSavedData> TYPE = new SavedDataType<>(
            "entropica_supernovae",
            SupernovaSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public SupernovaSavedData() {}

    public SupernovaSavedData(List<SupernovaEvent> events) {
        this.events.addAll(events);
    }

    public List<SupernovaEvent> getEvents() {
        return events;
    }

    public void setEvents(List<SupernovaEvent> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        setDirty();
    }

    public static SupernovaSavedData get(ServerLevel level) {
        SupernovaSavedData data = level.getDataStorage().computeIfAbsent(TYPE);
        if (SupernovaManager.getAllEvents().isEmpty() && !data.getEvents().isEmpty()) {
            SupernovaManager.setClientEvents(data.getEvents());
        }
        return data;
    }
}
