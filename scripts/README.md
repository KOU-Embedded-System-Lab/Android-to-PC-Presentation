Creating Slide From PDF
=======================

convert "SLIDE.PDF" to drawable_slide in "MY_SLIDE" directory

    convert -scale 1280x800 -density 300 -quality 100 SLIDE.PDF MY_SLIDE/x.png
    bash create_empty_draw.sh png/

caution: filename "x.png" hardcoded, use "x.png" in command.


Creating Empty Slide
====================

create empty 60 page drawable_slide in "MY_SLIDE" directory:

    bash create_empty_slide.sh MY_SLIDE/ 60
