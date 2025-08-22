package bikerboys.pivot.enums;

public enum DisplayType {
    NONE( "none"),
    THIRD_PERSON_LEFT_HAND("thirdperson_lefthand"),
    THIRD_PERSON_RIGHT_HAND("thirdperson_righthand"),
    FIRST_PERSON_LEFT_HAND("firstperson_lefthand"),
    FIRST_PERSON_RIGHT_HAND("firstperson_righthand"),
    HEAD("head"),
    GUI("gui"),
    GROUND("ground"),
    FIXED("fixed");


    private String id;

    DisplayType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }
}
