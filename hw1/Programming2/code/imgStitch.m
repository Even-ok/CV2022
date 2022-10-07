function file = imgStitch( img1, img2, trans, fileName )
% imgStitch: Stitching the images using the transformation matrix
%
% INPUT: 
%  img1 
%  img2 
%  trans - the transformation matric which is the output of RANSAC
%  fileName - the name of the stitching image
%
% OUTPUT: 
%   file - the stitching image

    if ~exist('fileName', 'var'),
        fileName = 'Stitching.jpg';
    end

%% Outbounds of panorama image
    outBounds = zeros(2,2);
    outBounds(1,:) = [0 0];
    outBounds(2,:) = -Inf;
    
    % Estimate the possible size
    [rows,cols,~] = size(img1);
    rows = 2 * rows;
    cols = 2 * cols;


    % Choose the second image as the reference
    ref = img2; 
    % Find output bounds for affine transformation  
    tmpBounds = findbounds(maketform('affine', trans'), [1 1; cols rows]);
    outBounds(1,:) = min(outBounds(1,:),tmpBounds(1,:));
    outBounds(2,:) = max(outBounds(2,:),tmpBounds(2,:));

%% Stitch the images.
    XLimit = round(outBounds(:,1)');
    YLimit = round(outBounds(:,2)');
    % Expand the image size
    Pano = imtransform( im2double(ref), maketform('affine', eye(3)), 'bilinear', ...
                    'XData', XLimit, 'YData', YLimit, ...
                    'FillValues', NaN, 'XYScale',1);
                
    Tform = maketform('affine', trans');
    leftImg = imtransform(im2double(img1), Tform, 'bilinear', ...
                        'XData', XLimit, 'YData', YLimit, ...
                        'FillValues', NaN, 'XYScale',1);
                    
    result_mask = ~isnan(Pano(:,:,1));
    temp_mask = ~isnan(leftImg(:,:,1));
	add_mask = temp_mask & (~result_mask);
    
    % Stitch two images
    for c = 1 : size(Pano,3),
        currentImg = Pano(:,:,c);
        tempImg = leftImg(:,:,c);
        currentImg(add_mask) = tempImg(add_mask);
        Pano(:,:,c) = currentImg;
    end

%% Cropping the final panorama to leave out black spaces.
    [I, J] = ind2sub([size(Pano, 1), size(Pano, 2)], find(~isnan(Pano(:, :, 1))));
    upper = max(min(I)-1, 1);
    lower = min(max(I)+1, size(Pano, 1));
    left = max(min(J)-1, 1);
    right = min(max(J)+1, size(Pano, 2));
    Pano = Pano(upper:lower, left:right,:);
    file = Pano;
    
    imwrite(file, fileName);
    
end