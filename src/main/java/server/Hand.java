package server;

import server.game.cards.Card;

public class Hand {

    private Card card1;
    private Card card2;

    Hand(){
        this.card1 = null;
        this.card2 = null;
    }

    public Card getCard1(){
        return this.card1;
    }

    public Card getCard2(){
        return this.card2;
    }

    public void setCard1(Card card){
        this.card1 = card;
    }

    public void setCard2(Card card){
        this.card2 = card;
    }

    public int getHandScore(){
        int score;

        if(this.card1 == null && this.card2 == null){
            return 0;
        }

        if(this.card1 == null) {
            score = this.card2.getCardNumber();
            return score;
        }

        if(this.card2 == null) {
            score = this.card1.getCardNumber();
            return score;
        }

        score = this.card1.getCardNumber() + this.card2.getCardNumber();
        return score;

    }

    public void addToHand(Card card){

        if(this.card1 == null) {
            setCard1(card);
        } else setCard2(card);
    }

    public void removeFromHand(Card card){
        if(this.card1 == card){
            setCard1(null);
        } else setCard2(null);
    }

    public void clearHand(){
        this.card1 = null;
        this.card2 = null;
    }

    public  Card discardHand(){

        if(this.card1 == null && this.card2 == null){
            return null;
        }

        if(this.card2 == null){
            return card1;
        }

        if(this.card1 == null){
            return card2;
        }

        return null;

    }

    @Override
    public String toString() {

        if(this.card1 == null && this.card2 == null){
            return "Hand is empty";
        }

        if(this.card2 == null){
            return "Hand - 1.Card: " + this.card1.toString();
        }

        if(this.card1 == null){
            return  "Hand - 2.Card: " + this.card2.toString();
        }

        return "Hand - 1.Card: " + this.card1.toString() + " 2.Card: " + this.card2.toString();

    }
}
