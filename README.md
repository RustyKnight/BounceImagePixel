# BounceImagePixel

A simple animation test

Inspired by [Click to form an image](https://codepen.io/allanpope/pen/LVWYYd) and [move dot between 2 points jframe](https://stackoverflow.com/questions/70718553/move-dot-between-2-points-jframe/70718933#70718933) making use of my personal animation library, [SuperSimpleSwingAnimationFramework
](https://github.com/RustyKnight/SuperSimpleSwingAnimationFramework)

**This is an experiment**

<img src="Bouncy.gif">

# Requirements

![Java](https://img.shields.io/badge/Java-16.0.2-orange) ![Netbeans](https://img.shields.io/badge/Netbeans-12.4-orange)

# Why?

Why mot?  This is an experiment in animation, it's an experiment in my "animation playground" library, it's annoyingly fun.

One thing to remember, this example is animating the pixles of a 300x300 image (90000 individual objects), which I think is quite neat

# What's the purpose of the "random delay"

The random delay is there to prevent a pixel wave.  Where the center of the circle is clear and all the pixles "fall in".  Maybe a variable speed might be a better idea, but I did find this produced other side effects.

# Optimisations

There is a lot of room for optimisation.  

1. We could reduce the number of pixels which are actually been moved.  I experiement with only moving every nth pixel and fading the rest, but since I could get all the pixels to move at once, I felt it wasn't required at this time
2. ~~When reforming the image, stop the pixels where they are and then move them to the image position.  Currently, a pixel is allowed to complete it's current animation path before it's moved to its image position.  Good, bad, indifferent, this might make the reconstituion of the image faster.~~
3. ~~Variable speed for each pixel.  This can make it a bit more of mess, but allowing a slight variability in the speed of each pixel might allow for the removal of the random delay been used to "randmise" the display~~
4. Instead of starting with the image formed, it could be possible to place each pixel at their starting point on the circle and simple have the animation "run"
5. Only a small subset of pixels could be running at any one time, all the other pixels would be held at their "target" points, a little like a pool, so when a pixel reaches it's destination, it's place back in the pool of avaliable pixels and new pixels are drawen out of it.  This might reduce the "static noise" of that the animation suffers from right now

*nb: 2 & 3 are now basically implemented*
