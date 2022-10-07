
%% Clear all
clc; clear; close all; clc;

%% Load image
img1_dir = '../picture/sse1.bmp';
img2_dir = '../picture/sse2.bmp';
img1 = imread(img1_dir);
img2 = imread(img2_dir);
%imshow(img1);

% Resize the picture to avoid out of memory
if max(size(img1))>1000
    img1 = imresize(img1,0.6);
end
if max(size(img2))>1000
    img2 = imresize(img2,0.6);
end
% Rename the stiching image here
finalImage = 'RainierStitching.png';

%% Feature extraction
dcp1 = []; % descriptor
dpc2 = [];
interestPoint1 = [];
interestPoint2 = [];
siftImg1 = single(rgb2gray(img1));
siftImg2 = single(rgb2gray(img2));


% f: [x,y,s,th]  
% x,y: interest point location
% s: characristic scale
% th: dominant orientation
% d: 128D(4*4*8) element vector 
[f1,d1] = vl_sift(siftImg1);
[f2,d2] = vl_sift(siftImg2);

interestPoint1 = double(f1(1:2,:)');
interestPoint2 = double(f2(1:2,:)');
dcp1 = double(d1');
dcp2 = double(d2');

%% Draw picture
%the matches and the squared Euclidean distance between the matches.
[matches, scores] = vl_ubcmatch(d1,d2) ;

[drop, perm] = sort(scores, 'descend') ;
matches = matches(:, perm) ;
scores  = scores(perm) ;

figure(1) ; clf ;
imagesc(cat(2, img1, img2)) ;
axis image off ;

figure(2) ; clf ;
imagesc(cat(2, img1, img2)) ;

xa = f1(1,matches(1,:)) ;
xb = f2(1,matches(2,:)) + size(img1,2) ;
ya = f1(2,matches(1,:)) ;
yb = f2(2,matches(2,:)) ;

hold on ;
h = line([xa ; xb], [ya ; yb]) ;
set(h,'linewidth', 1, 'color', 'b') ;

vl_plotframe(f1(:,matches(1,:))) ;
f2(1,:) = f2(1,:) + size(img1,2) ;
vl_plotframe(f2(:,matches(2,:))) ;
axis image off ;


%% Compute Transformation (Array H) 
% Match one SIFT descriptors to another.
match = SIFTMatching(dcp1, dcp2, 0.7);
trans = RANSAC(interestPoint1, interestPoint2, match);

%% Make Panoramic image

imgStitch(img1, img2, trans, finalImage);
disp(['The completed picture path is  ' finalImage]);
figure(3) ; clf ;
imshow(imread(finalImage));

