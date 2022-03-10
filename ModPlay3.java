
public class ModPlay3
{
	private static final int MAX_SAMPLES = 32;
	private static final int MAX_CHANNELS = 8;
	private static final int FIXED_POINT_SHIFT = 13;
	private static final int FIXED_POINT_ONE = 1 << FIXED_POINT_SHIFT;
	
	private static final int[] FILTER_COEFFS =
	{
		-512, 0, 4096, 8192, 4096, 0, -512
	};
	
	private static final short[] KEY_TO_PERIOD =
	{
		1814, /*
		 C-0   C#0   D-0   D#0   E-0   F-0   F#0   G-0   G#0   A-1  A#1  B-1 */
		1712, 1616, 1524, 1440, 1356, 1280, 1208, 1140, 1076, 1016, 960, 907,
		 856,  808,  762,  720,  678,  640,  604,  570,  538,  508, 480, 453,
		 428,  404,  381,  360,  339,  320,  302,  285,  269,  254, 240, 226,
		 214,  202,  190,  180,  170,  160,  151,  143,  135,  127, 120, 113,
		 107,  101,   95,   90,   85,   80,   75,   71,   67,   63,  60,  56,
		  53,   50,   47,   45,   42,   40,   37,   35,   33,   31,  30,  28, 26
	};
	
	private static final short[] FINE_TUNE =
	{
		8192, 8133, 8075, 8016, 7959, 7902, 7845, 7788,
		8679, 8617, 8555, 8493, 8432, 8371, 8311, 8251
	};
	
	private static final short[] VIBRATO =
	{
		0,    24,  49,  74, 97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253,
		255, 253, 250, 244,235, 224, 212, 197, 180, 161, 141, 120,  97,  74,  49,  24
	};
	
	private String songName;
	private String[] instrumentNames = new String[ MAX_SAMPLES ];
	private int numChannels;
	private int patChannels;
	private int songLength;
	private int c2Rate;
	private byte[] sequence;
	private byte[] patternData;
	private byte[][] sampleData = new byte[ MAX_SAMPLES ][];
	private int[] sampleFineTune = new int[ MAX_SAMPLES ];
	private int[] sampleVolume = new int[ MAX_SAMPLES ];
	private int[] sampleLoopStart = new int[ MAX_SAMPLES ];
	private int[] sampleLoopLength = new int[ MAX_SAMPLES ];
	private int[] channelInstrument = new int[ MAX_CHANNELS ];
	private int[] channelAssigned = new int[ MAX_CHANNELS ];
	private int[] channelEffect = new int[ MAX_CHANNELS ];
	private int[] channelParameter = new int[ MAX_CHANNELS ];
	private int[] channelVolume = new int[ MAX_CHANNELS ];
	private int[] channelPanning = new int[ MAX_CHANNELS ];
	private int[] channelPeriod = new int[ MAX_CHANNELS ];
	private int[] channelSamplePos = new int[ MAX_CHANNELS ];
	private int[] channelFrequency = new int[ MAX_CHANNELS ];
	private int[] channelArpeggio = new int[ MAX_CHANNELS ];
	private int[] channelVibrato = new int[ MAX_CHANNELS ];
	private int[] channelVibratoSpeed = new int[ MAX_CHANNELS ];
	private int[] channelVibratoDepth = new int[ MAX_CHANNELS ];
	private int[] channelVibratoPhase = new int[ MAX_CHANNELS ];
	private int[] channelPortaPeriod = new int[ MAX_CHANNELS ];
	private int[] channelPortaSpeed = new int[ MAX_CHANNELS ];
	private int[] channelTremolo = new int[ MAX_CHANNELS ];
	private int[] channelTremoloSpeed = new int[ MAX_CHANNELS ];
	private int[] channelTremoloDepth = new int[ MAX_CHANNELS ];
	private int[] channelPatternLoopRow = new int[ MAX_CHANNELS ];
	private int[] channelSampleOffset = new int[ MAX_CHANNELS ];
	private int currentSequencePos;
	private int nextSequencePos;
	private int currentRow;
	private int nextRow;
	private int currentTick;
	private int ticksPerRow;
	private int tempo;
	private int effectCounter;
	private int patternLoopCount;
	private int patternLoopChannel;
	private int mute;
	private boolean sequencerEnabled = true;
	private int[] rampBuf = new int[ 64 ];
	
	public ModPlay3( int numChannels )
	{
		songName = "";
		for( int idx = 1; idx < MAX_SAMPLES; idx++ )
		{
			instrumentNames[ idx ] = "";
			sampleVolume[ idx ] = 64;
			sampleData[ idx ] = new byte[ 0 ];
		}
		songLength = 1;
		sequence = new byte[ 1 ];
		setNumChannels( numChannels );
		setPatternData( new byte[ numChannels * 4 * 64 ], numChannels );
		setSequencePos( 0, 0 );
	}
	
	/* If 'soundtracker' is true, the Module Data is assumed to be in the original Ultimate Soundtracker format. */
	public ModPlay3( java.io.InputStream moduleData, boolean soundtracker ) throws java.io.IOException
	{
		songName = readString( moduleData, 20 );
		int numSamples = soundtracker ? 16 : 32;
		int[] sampleLengths = new int[ numSamples ];
		for( int idx = 1; idx < numSamples; idx++ )
		{
			instrumentNames[ idx ] = readString( moduleData, 22 );
			sampleLengths[ idx ] = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			sampleFineTune[ idx ] = moduleData.read() & ( soundtracker ? 0 : 0xF );
			sampleVolume[ idx ] = moduleData.read() & 0x7F;
			int loopStart = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * ( soundtracker ? 1 : 2 );
			int loopLength = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			if( loopLength < 4 || loopStart > sampleLengths[ idx ] )
			{
				loopStart = sampleLengths[ idx ];
			}
			if( loopStart + loopLength > sampleLengths[ idx ] )
			{
				loopLength = sampleLengths[ idx ] - loopStart;
			}
			sampleLoopStart[ idx ] = loopStart * FIXED_POINT_ONE;
			sampleLoopLength[ idx ] = loopLength * FIXED_POINT_ONE;
		}
		for( int idx = numSamples; idx < MAX_SAMPLES; idx++ )
		{
			instrumentNames[ idx ] = "";
			sampleVolume[ idx ] = 64;
			sampleData[ idx ] = new byte[ 0 ];
		}
		songLength = moduleData.read() & 0x7F;
		if( songLength < 1 )
		{
			songLength = 1;
		}
		int restart = moduleData.read() & 0x7F;
		int numPatterns = 0;
		sequence = readBytes( moduleData, 128 );
		for( int idx = 0; idx < 128; idx++ )
		{
			if( numPatterns < sequence[ idx ] + 1 ) 
			{
				numPatterns = sequence[ idx ] + 1;
			}
		}
		String modType = soundtracker ? "M.K." : readString( moduleData, 4 );
		if( modType.equals( "M.K." ) || modType.equals( "M!K!" ) || modType.equals( "FLT4" ) )
		{
			setNumChannels( 4 );
		}
		else if( modType.equals( "CD81" ) || modType.equals( "OKTA" ) )
		{
			setNumChannels( 8 );
		}
		else if( modType.length() > 0 && modType.substring( 1 ).equals( "CHN" ) )
		{
			setNumChannels( modType.charAt( 0 ) - '0' );
		}
		if( numChannels < 1 || numChannels > MAX_CHANNELS )
		{
			throw new IllegalArgumentException( "Module not recognised!" );
		}
		setPatternData( readBytes( moduleData, numChannels * 4 * 64 * numPatterns ), numChannels );
		for( int idx = 0; idx < patternData.length; idx += 4 )
		{
			int key = periodToKey( ( ( patternData[ idx ] & 0xF ) << 8 ) | ( patternData[ idx + 1 ] & 0xFF ) );
			int instrument = ( soundtracker ? 0 : patternData[ idx ] & 0x10 ) | ( ( patternData[ idx + 2 ] >> 4 ) & 0xF );
			int effect = patternData[ idx + 2 ] & 0xF;
			int param1 = ( patternData[ idx + 3 ] >> 4 ) & 0xF;
			int param2 = patternData[ idx + 3 ] & 0xF;
			if( soundtracker )
			{
				if( effect == 1 )
				{
					effect = 0;
				}
				else if( effect == 2 && ( param1 - param2 ) < 0 )
				{
					effect = 1;
					param2 = param2 - param1;
					param1 = 0;
				} 
				else if( effect == 2 )
				{
					effect = 2;
					param2 = param1 - param2;
					param1 = 0;
				}
				else
				{
					effect = param1 = param2 = 0;
				}
			}
			patternData[ idx ] = ( byte ) key;
			patternData[ idx + 1 ] = ( byte ) instrument;
			patternData[ idx + 2 ] = ( byte ) effect;
			patternData[ idx + 3 ] = ( byte ) ( ( param1 << 4 ) | ( param2 & 0xF ) );
		}
		for( int idx = 1; idx < numSamples; idx++ )
		{
			sampleData[ idx ] = readBytes( moduleData, sampleLengths[ idx ] );
			if( soundtracker && sampleLoopLength[ idx ] > 0 )
			{
				byte[] data = sampleData[ idx ];
				int loopStart = sampleLoopStart[ idx ] >> FIXED_POINT_SHIFT;
				int loopLength = sampleLoopLength[ idx ] >> FIXED_POINT_SHIFT;
				System.arraycopy( data, loopStart, data, 0, loopLength );
				sampleLoopStart[ idx ] = 0;
			}
		}
		setSequencePos( 0, 0 );
	}
	
	public void writeModule( java.io.OutputStream outputStream ) throws java.io.IOException
	{
		byte[] header = new byte[ 1084 ];
		writeAscii( songName, header, 0, 20 );
		for( int idx = 1; idx < MAX_SAMPLES; idx++ )
		{
			writeAscii( instrumentNames[ idx ], header, idx * 30 - 10, 22 );
			header[ idx * 30 + 12 ] = ( byte ) ( sampleData[ idx ].length >> 9 );
			header[ idx * 30 + 13 ] = ( byte ) ( sampleData[ idx ].length >> 1 );
			header[ idx * 30 + 14 ] = ( byte ) sampleFineTune[ idx ];
			header[ idx * 30 + 15 ] = ( byte ) sampleVolume[ idx ];
			header[ idx * 30 + 16 ] = ( byte ) ( sampleLoopStart[ idx ] >> FIXED_POINT_SHIFT + 9 );
			header[ idx * 30 + 17 ] = ( byte ) ( sampleLoopStart[ idx ] >> FIXED_POINT_SHIFT + 1 );
			header[ idx * 30 + 18 ] = ( byte ) ( sampleLoopLength[ idx ] >> FIXED_POINT_SHIFT + 9 );
			header[ idx * 30 + 19 ] = ( byte ) ( sampleLoopLength[ idx ] >> FIXED_POINT_SHIFT + 1 );
		}
		header[ 950 ] = ( byte ) songLength;
		System.arraycopy( sequence, 0, header, 952, songLength );
		int numPatterns = 0;
		for( int idx = 0; idx < songLength; idx++ )
		{
			if( numPatterns <= sequence[ idx ] ) 
			{
				numPatterns = sequence[ idx ] + 1;
			}
		}
		if( numChannels == 4 )
		{
			writeAscii( numPatterns > 64 ? "M!K!" : "M.K.", header, 1080, 4 );
		}
		else
		{
			header[ 1080 ] = ( byte ) ( '0' + numChannels );
			writeAscii( "CHN", header, 1081, 3 );
		}
		outputStream.write( header );
		byte[] outputData = new byte[ numChannels * 4 * 64 * numPatterns ];
		for( int row = 0, rows = numPatterns * 64; row < rows; row++ )
		{
			for( int chn = 0; chn < numChannels; chn++ )
			{
				int patIdx = ( row * patChannels + chn ) * 4;
				int outIdx = ( row * numChannels + chn ) * 4;
				int period = keyToPeriod( patternData[ patIdx ] & 0xFF, 0 );
				int instrument = patternData[ patIdx + 1 ] & 0x1F;
				outputData[ outIdx ] = ( byte ) ( ( instrument & 0x10 ) | ( ( period >> 8 ) & 0xF ) );
				outputData[ outIdx + 1 ] = ( byte ) ( period & 0xFF );
				outputData[ outIdx + 2 ] = ( byte ) ( ( ( instrument & 0xF ) << 4 ) | ( patternData[ patIdx + 2 ] & 0xF ) );
				outputData[ outIdx + 3 ] = patternData[ patIdx + 3 ];
			}
		}
		outputStream.write( outputData );
		for( int idx = 1; idx < MAX_SAMPLES; idx++ )
		{
			outputStream.write( sampleData[ idx ], 0, sampleData[ idx ].length & -2 );
		}
	}
	
	public String getSongName()
	{
		return songName;
	}
	
	public void setSongName( String name )
	{
		songName = name.length() > 20 ? name.substring( 0, 20 ) : name;
	}
	
	public String getInstrumentName( int idx )
	{
		return instrumentNames[ idx ];
	}
	
	public void setInstrumentName( int idx, String name )
	{
		instrumentNames[ idx ] = name.length() > 22 ? name.substring( 0, 22 ) : name;
	}
	
	public int getSampleLength( int idx )
	{
		return sampleData[ idx ].length;
	}
	
	public int getSampleVolume( int idx )
	{
		return sampleVolume[ idx ];
	}
	
	public void setSampleVolume( int idx, int volume )
	{
		sampleVolume[ idx ] = ( volume < 0 || volume > 64 ) ? 64 : volume;
	}
	
	public int getSampleFinetune( int idx )
	{
		int finetune = sampleFineTune[ idx ];
		return finetune < 8 ? finetune : finetune - 16;
	}
	
	public void setSampleFinetune( int idx, int finetune ) 
	{
		if( finetune < -8 || finetune > 7 )
		{
			finetune = 0;
		}
		sampleFineTune[ idx ] = finetune < 0 ? finetune + 16 : finetune;
	}
	
	public int getSampleLoopStart( int idx )
	{
		return sampleLoopStart[ idx ] >> FIXED_POINT_SHIFT;
	}
	
	public int getSampleLoopLength( int idx )
	{
		return sampleLoopLength[ idx ] >> FIXED_POINT_SHIFT;
	}
	
	public void setSampleLoop( int idx, int loopStart, int loopLength )
	{
		int sampleLength = sampleData[ idx ].length;
		if( loopStart < 0 || loopStart > sampleLength )
		{
			loopStart = sampleLength;
		}
		if( loopLength < 4 || loopStart + loopLength > sampleLength )
		{
			loopLength = sampleLength - loopStart;
		}
		sampleLoopStart[ idx ] = ( loopStart & -2 ) * FIXED_POINT_ONE;
		sampleLoopLength[ idx ] = ( loopLength & -2 ) * FIXED_POINT_ONE;
	}
	
	public byte[] getSampleData( int idx )
	{
		return sampleData[ idx ];
	}
	
	public void setSampleData( int idx, byte[] data )
	{
		sampleData[ idx ] = new byte[ ( data.length > 0x1FFFE ? 0x1FFFE : data.length ) & -2 ];
		System.arraycopy( data, 0, sampleData[ idx ], 0, sampleData[ idx ].length );
		setSampleLoop( idx, sampleData[ idx ].length, 0 );
	}
	
	public int getNumChannels()
	{
		return numChannels;
	}
	
	public void setNumChannels( int numChannels )
	{
		this.numChannels = numChannels;
		this.c2Rate = numChannels == 4 ? 8287 : 8363;
	}
	
	public int getSongLength()
	{
		return songLength;
	}
	
	public int getPattern( int sequencePos )
	{
		return sequencePos < songLength ? sequence[ sequencePos ] : 0;
	}
	
	public void setSequence( byte[] sequence )
	{
		this.sequence = sequence;
		this.songLength = sequence.length;
	}
	
	public byte[] getPatternData()
	{
		return patternData;
	}
	
	public void setPatternData( byte[] patternData, int patChannels )
	{
		this.patternData = patternData;
		this.patChannels = patChannels;
		if( numChannels > patChannels )
		{
			setNumChannels( patChannels );
		}
	}
	
	public int getRow()
	{
		return currentRow;
	}
	
	public int getSequencePos()
	{
		return currentSequencePos;
	}
	
	/* Set the position in the sequence. The tempo is reset to the default. */
	public void setSequencePos( int sequencePos, int row )
	{
		if( sequencePos < 0 || sequencePos >= songLength )
		{
			sequencePos = 0;
		}
		if( row < 0 || row > 63 )
		{
			row = 0;
		}
		clear( channelInstrument );
		clear( channelAssigned );
		clear( channelEffect );
		clear( channelParameter );
		clear( channelVolume );
		clear( channelPanning );
		clear( channelPeriod );
		clear( channelSamplePos );
		clear( channelFrequency );
		clear( channelArpeggio );
		clear( channelVibrato );
		clear( channelVibratoSpeed );
		clear( channelVibratoDepth );
		clear( channelVibratoPhase );
		clear( channelPortaPeriod );
		clear( channelPortaSpeed );
		clear( channelTremolo );
		clear( channelTremoloSpeed );
		clear( channelTremoloDepth );
		clear( channelPatternLoopRow );
		clear( channelSampleOffset );
		currentSequencePos = nextSequencePos = sequencePos;
		currentRow = nextRow = row;
		currentTick = 0;
		ticksPerRow = 6;
		tempo = 125;
		effectCounter = 0;
		patternLoopCount = 0;
		patternLoopChannel = 0;
		clear( rampBuf );
		for( int chn = 0; chn < MAX_CHANNELS; chn += 4 )
		{
			channelPanning[ chn ] = channelPanning[ chn + 3 ] = FIXED_POINT_ONE / 5;
			channelPanning[ chn + 1 ] = channelPanning[ chn + 2 ] = FIXED_POINT_ONE * 4 / 5;
		}
		tick();
	}
	
	/* Seek to the specified position in the sequence. */
	public void seek( int sequencePos, int row, int sampleRate )
	{
		if( sequencePos < 0 || sequencePos >= songLength )
		{
			sequencePos = 0;
		}
		if( row < 0 || row > 63 )
		{
			row = 0;
		}
		setSequencePos( 0, 0 );
		while( currentSequencePos < sequencePos || currentRow < row )
		{
			int count = ( sampleRate * 5 ) / ( tempo * 2 );
			for( int chn = 0; chn < numChannels; chn++ )
			{
				updateSamplePos( chn, count, sampleRate );
			}
			boolean songEnd = tick();
			if( songEnd )
			{
				setSequencePos( sequencePos, row );
				return;
			}
		}
	}
	
	public int getMute()
	{
		return mute;
	}
	
	public void setMute( int bitmask )
	{
		mute = bitmask;
	}
	
	public boolean getSequencer()
	{
		return sequencerEnabled;
	}
	
	public void setSequencer( boolean enabled )
	{
		sequencerEnabled = enabled;
	}
	
	public void trigger( int channel, int instrument, int key, int volume )
	{
		if( instrument > 0 )
		{
			channelInstrument[ channel ] = instrument;
			int period = keyToPeriod( key, sampleFineTune[ instrument ] );
			channelFrequency[ channel ] = period > 0 ? c2Rate * 428 / period : c2Rate;
			channelSamplePos[ channel ] = 0;
		}
		channelVolume[ channel ] = volume;
	}
	
	/* Returns number of stereo samples produced.
	   Output buffer must be of length sampleRate / 5. */
	public int getAudio( int sampleRate, int[] output )
	{
		int count = ( sampleRate * 5 ) / ( tempo * 2 );
		for( int idx = 0, end = ( count + 32 ) * 2; idx < end; idx++ )
		{
			output[ idx ] = 0;
		}
		for( int chn = 0; chn < numChannels; chn++ )
		{
			if( ( mute & ( 1 << chn ) ) == 0 )
			{
				resample( chn, output, count + 32, sampleRate );
			}
			updateSamplePos( chn, count, sampleRate );
		}
		volumeRamp( output, count );
		if( sequencerEnabled )
		{
			tick();
		}
		return count;
	}
	
	private void updateSamplePos( int channel, int count, int sampleRate )
	{
		int instrument = channelInstrument[ channel ];
		int loopStart = sampleLoopStart[ instrument ];
		int loopLength = sampleLoopLength[ instrument ];
		int step = channelFrequency[ channel ] * FIXED_POINT_ONE / sampleRate;
		int samplePos = channelSamplePos[ channel ] + step * count;
		if( samplePos >= loopStart + loopLength )
		{
			if( loopLength > 0 )
			{
				samplePos = loopStart + ( samplePos - loopStart ) % loopLength;
			}
			else
			{
				samplePos = loopStart;
			}
		}
		channelSamplePos[ channel ] = samplePos;
	}
	
	private void resample( int channel, int[] output, int count, int sampleRate )
	{
		int instrument = channelInstrument[ channel ];
		int loopLength = sampleLoopLength[ instrument ];
		int loopEnd = sampleLoopStart[ instrument ] + loopLength;
		int samplePos = channelSamplePos[ channel ];
		int step = channelFrequency[ channel ] * FIXED_POINT_ONE / sampleRate;
		int volume = channelVolume[ channel ];
		int panning = channelPanning[ channel ];
		for( int idx = 0, end = count * 2; idx < end; idx += 2 )
		{
			if( samplePos >= loopEnd )
			{
				if( loopLength <= 0 )
				{
					return;
				}
				while( samplePos >= loopEnd )
				{
					samplePos -= loopLength;
				}
			}
			int amplitude = sampleData[ instrument ][ samplePos >> FIXED_POINT_SHIFT ] * volume;
			output[ idx ] += ( amplitude * ( FIXED_POINT_ONE - panning ) ) >> FIXED_POINT_SHIFT;
			output[ idx + 1 ] += ( amplitude * panning ) >> FIXED_POINT_SHIFT;
			samplePos += step;
		}
	}

	private void volumeRamp( int[] output, int count )
	{
		for( int idx = 0; idx < 32; idx++ )
		{
			output[ idx * 2 ] = ( output[ idx * 2 ] * idx + rampBuf[ idx * 2 ] * ( 32 - idx ) ) / 32;
			rampBuf[ idx * 2 ] = output[ ( count + idx ) * 2 ];
			output[ idx * 2 + 1 ] = ( output[ idx * 2 + 1 ] * idx + rampBuf[ idx * 2 + 1 ] * ( 32 - idx ) ) / 32;
			rampBuf[ idx * 2 + 1 ] = output[ ( count + idx ) * 2 + 1 ];
		}
	}

	private boolean row()
	{
		boolean songEnd = nextSequencePos < currentSequencePos || ( nextSequencePos == currentSequencePos && nextRow <= currentRow && patternLoopCount <= 0 );
		boolean patternBreak = false;
		if( nextSequencePos < songLength )
		{
			currentSequencePos = nextSequencePos;
		}
		else
		{
			currentSequencePos = nextSequencePos = 0;
			songEnd = true;
		}
		if( nextRow < 64 )
		{
			currentRow = nextRow;
		}
		else
		{
			currentRow = 0;
		}
		currentTick = ticksPerRow;
		nextRow = currentRow + 1;
		if( nextRow > 63 )
		{
			nextRow = 0;
			nextSequencePos = currentSequencePos + 1;
			patternBreak = true;
		}
		for( int chn = 0; chn < numChannels; chn++ )
		{
			int rowOffset = sequence[ currentSequencePos ] * 64 + currentRow;
			int patternDataOffset = ( rowOffset * patChannels + chn ) * 4;
			int key = patternData[ patternDataOffset ] & 0xFF;
			int instrument = patternData[ patternDataOffset + 1 ] & 0xFF;
			int effect = patternData[ patternDataOffset + 2 ] & 0xF;
			int parameter = patternData[ patternDataOffset + 3 ] & 0xFF;
			if( effect == 0xE )
			{
				effect = 0xE0 | ( ( parameter >> 4 ) & 0xF );
				parameter = parameter & 0xF;
			}
			if( instrument > 0 )
			{
				channelAssigned[ chn ] = instrument;
				channelVolume[ chn ] = sampleVolume[ instrument ];
				channelSampleOffset[ chn ] = 0;
				if( sampleLoopLength[ instrument ] > 0 && channelInstrument[ chn ] > 0 )
				{
					channelInstrument[ chn ] = instrument;
				}
			}
			if( effect == 0x9 )
			{
				channelSampleOffset[ chn ] = parameter * 256 * FIXED_POINT_ONE;
			}
			if( key > 0 )
			{
				channelPortaPeriod[ chn ] = keyToPeriod( key, sampleFineTune[ channelAssigned[ chn ] ] );
				if( effect != 0x3 && effect != 0x5 && !( effect == 0xED && parameter > 0 ) )
				{
					channelInstrument[ chn ] = channelAssigned[ chn ];
					channelPeriod[ chn ] = channelPortaPeriod[ chn ];
					channelSamplePos[ chn ] = channelSampleOffset[ chn ];
					channelVibratoPhase[ chn ] = 0;
				}
			}
			channelEffect[ chn ] = effect;
			channelParameter[ chn ] = parameter;
			channelArpeggio[ chn ] = 0;
			channelVibrato[ chn ] = 0;
			channelTremolo[ chn ] = 0;
			switch( effect )
			{
				case 0x0: /* Arpeggio. */
				case 0x1: /* Portamento up. */
				case 0x2: /* Portamento down. */
					break;
				case 0x3: /* Tone portamento. */
					if( parameter > 0 )
					{
						channelPortaSpeed[ chn ] = parameter;
					}
					break;
				case 0x4: /* Vibrato. */
					if( ( parameter & 0xF0 ) > 0 )
					{
						channelVibratoSpeed[ chn ] = ( parameter & 0xF0 ) >> 4;
					}
					if( ( parameter & 0xF ) > 0 )
					{
						channelVibratoDepth[ chn ] = parameter & 0xF;
					}
					vibrato( chn );
					break;
				case 0x5: /* Tone porta + Volume slide. */
					break;
				case 0x6: /* Vibrato + Volume slide. */
					vibrato( chn );
					break;
				case 0x7: /* Tremolo. */
					if( ( parameter & 0xF0 ) > 0 )
					{
						channelTremoloSpeed[ chn ] = ( parameter & 0xF0 ) >> 4;
					}
					if( ( parameter & 0xF ) > 0 )
					{
						channelTremoloDepth[ chn ] = parameter & 0xF;
					}
					tremolo( chn );
					break;
				case 0x8: /* Set panning. */
					if( numChannels > 4 )
					{
						if( parameter > 128 )
						{
							channelPanning[ chn ] = FIXED_POINT_ONE / 2;
						}
						else
						{
							channelPanning[ chn ] = parameter * FIXED_POINT_ONE / 128;
						}
					}
					break;
				case 0x9: /* Set sample offset. */
					break;
				case 0xA: /* Volume slide. */
					break;
				case 0xB: /* Pattern jump. */
					nextSequencePos = parameter;
					if( !patternBreak )
					{
						nextRow = 0;
						patternBreak = true;
					}
					break;
				case 0xC: /* Set volume. */
					channelVolume[ chn ] = parameter;
					break;
				case 0xD: /* Pattern break. */
					if( !patternBreak )
					{
						nextSequencePos = currentSequencePos + 1;
						patternBreak = true;
					}
					nextRow = ( parameter >> 4 ) * 10 + ( parameter & 0xF );
					break;
				case 0xE: /* Remapped to 0xEx. */
					break;
				case 0xF: /* Set speed/tempo.*/
					if( channelParameter[ chn ] > 31 )
					{
						tempo = channelParameter[ chn ];
					}
					else if( channelParameter[ chn ] > 0 )
					{
						currentTick = ticksPerRow = channelParameter[ chn ];
					}
					break;
				case 0xE0: /* Set filter. */
					break;
				case 0xE1: /* Fine portamento up. */
					channelPeriod[ chn ] -= parameter;
					break;
				case 0xE2: /* Fine portamento down. */
					channelPeriod[ chn ] += parameter;
					break;
				case 0xE3: /* Glissando. */
				case 0xE4: /* Set vibrato waveform. */
				case 0xE5: /* Set finetune. */
					break;
				case 0xE6: /* Pattern loop. */
					if( channelPatternLoopRow[ chn ] < currentRow )
					{
						if( parameter > 0 )
						{
							if( patternLoopCount <= 0 )
							{
								patternLoopCount = parameter + 1;
								patternLoopChannel = chn;
							}
							if( patternLoopChannel == chn )
							{
								patternLoopCount--;
								if( patternLoopCount > 0 )
								{
									nextSequencePos = currentSequencePos;
									nextRow = channelPatternLoopRow[ chn ];
									patternBreak = false;
								}
								else
								{
									channelPatternLoopRow[ chn ] = currentRow + 1;
								}
							}
						}
						else
						{
							channelPatternLoopRow[ chn ] = currentRow;
						}
					}
					break;
				case 0xE7: /* Set tremolo waveform. */
				case 0xE8: /* Panning. */
				case 0xE9: /* Retrig. */
					break;
				case 0xEA: /* Fine volume slide up. */
					channelVolume[ chn ] += parameter;
					break;
				case 0xEB: /* Fine volume slide down. */
					channelVolume[ chn ] -= parameter;
					break;
				case 0xEC: /* Note cut. */
					break;
				case 0xED: /* Note delay. */
					if( key <= 0 )
					{
						channelParameter[ chn ] = 0;
					}
					break;
				case 0xEE: /* Pattern delay. */
					currentTick += ticksPerRow * parameter;
					break;
				case 0xEF: /* Invert loop. */
					break;
			}
		}
		if( patternBreak )
		{
			patternLoopCount = 0;
			patternLoopChannel = 0;
			for( int chn = 0; chn < numChannels; chn++ )
			{
				channelPatternLoopRow[ chn ] = 0;
			}
		}
		return songEnd;
	}
	
	private boolean tick()
	{
		boolean songEnd = false;
		if( --currentTick <= 0 )
		{
			songEnd = row();
			effectCounter = 0;
		}
		else
		{
			effectCounter++;
			for( int chn = 0; chn < numChannels; chn++ ) 
			{
				switch( channelEffect[ chn ] )
				{
					case 0x0: /* Arpeggio. */
						if( channelParameter[ chn ] > 0 )
						{
							switch( effectCounter % 3 )
							{
								default:
									channelArpeggio[ chn ] = 0;
									break;
								case 1:
									channelArpeggio[ chn ] = ( channelParameter[ chn ] >> 4 ) & 0xF;
									break;
								case 2:
									channelArpeggio[ chn ] = channelParameter[ chn ] & 0xF;
									break;
							}
						}
						break;
					case 0x1: /* Portamento up. */
						channelPeriod[ chn ] -= channelParameter[ chn ];
						break;
					case 0x2: /* Portamento down. */
						channelPeriod[ chn ] += channelParameter[ chn ];
						break;
					case 0x3: /* Tone portamento. */
						tonePortamento( chn );
						break;
					case 0x4: /* Vibrato. */
						vibrato( chn );
						break;
					case 0x5: /* Tone porta + Volume slide. */
						tonePortamento( chn );
						volumeSlide( chn );
						break;
					case 0x6: /* Vibrato + Volume slide. */
						vibrato( chn );
						volumeSlide( chn );
						break;
					case 0x7: /* Tremolo. */
						tremolo( chn );
						break;
					case 0xA: /* Volume slide. */
						volumeSlide( chn );
						break;
					case 0xE9: /* Retrig. */
						if( effectCounter % channelParameter[ chn ] == 0 )
						{
							channelSamplePos[ chn ] = 0;
						}
						break;
					case 0xEC: /* Note cut. */
						if( effectCounter == channelParameter[ chn ] )
						{
							channelVolume[ chn ] = 0;
						}
						break;
					case 0xED: /* Note delay. */
						if( effectCounter == channelParameter[ chn ] )
						{
							channelInstrument[ chn ] = channelAssigned[ chn ];
							channelPeriod[ chn ] = channelPortaPeriod[ chn ];
							channelSamplePos[ chn ] = channelSampleOffset[ chn ];
							channelVibratoPhase[ chn ] = 0;
						}
						break;
				}
			}
		}
		for( int chn = 0; chn < numChannels; chn++ ) 
		{
			/* Calculate volume and frequency. */
			int volume = channelVolume[ chn ];
			if( volume > 64 )
			{
				volume = channelVolume[ chn ] = 64;
			}
			else if( volume < 0 )
			{
				volume = channelVolume[ chn ] = 0;
			}
			volume = volume + channelTremolo[ chn ];
			if( volume > 64 )
			{
				volume = 64;
			}
			else if( volume < 0 )
			{
				volume = 0;
			}
			int period = channelPeriod[ chn ];
			if( period > 0 )
			{
				period = transpose( period + channelVibrato[ chn ], channelArpeggio[ chn ] );
				if( period < 28 )
				{
					period = 6848;
				}
				channelFrequency[ chn ] = c2Rate * 428 / period;
			}
		}
		return songEnd;
	}
	
	private void tonePortamento( int chn )
	{
		if( channelPeriod[ chn ] < channelPortaPeriod[ chn ] )
		{
			channelPeriod[ chn ] += channelPortaSpeed[ chn ];
			if( channelPeriod[ chn ] > channelPortaPeriod[ chn ] )
			{
				channelPeriod[ chn ] = channelPortaPeriod[ chn ];
			}
		}
		else
		{
			channelPeriod[ chn ] -= channelPortaSpeed[ chn ];
			if( channelPeriod[ chn ] < channelPortaPeriod[ chn ] )
			{
				channelPeriod[ chn ] = channelPortaPeriod[ chn ];
			}
		}
	}
	
	private void vibrato( int chn )
	{
		int phase = channelVibratoPhase[ chn ] & 0x3F;
		channelVibrato[ chn ] = ( VIBRATO[ phase & 0x1F ] * channelVibratoDepth[ chn ] ) >> 7;
		if( phase > 0x1F )
		{
			channelVibrato[ chn ] = -channelVibrato[ chn ];
		}
		channelVibratoPhase[ chn ] += channelVibratoSpeed[ chn ];
	}
	
	private void volumeSlide( int chn )
	{
		int up = ( channelParameter[ chn ] >> 4 ) & 0xF;
		int down = channelParameter[ chn ] & 0xF;
		channelVolume[ chn ] += up - down;
	}
	
	private void tremolo( int chn )
	{
		int phase = channelVibratoPhase[ chn ] & 0x3F;
		channelTremolo[ chn ] = ( VIBRATO[ phase & 0x1F ] * channelTremoloDepth[ chn ] ) >> 6;
		if( phase > 0x1F )
		{
			channelTremolo[ chn ] = -channelTremolo[ chn ];
		}
		channelVibratoPhase[ chn ] += channelTremoloSpeed[ chn ];
	}
	
	private static int keyToPeriod( int key, int fineTune )
	{
		int period = 0;
		if( key > 0 && key < 73 )
		{
			period = ( KEY_TO_PERIOD[ key ] * FINE_TUNE[ fineTune & 0xF ] ) >> FIXED_POINT_SHIFT - 1;
		}
		return ( period >> 1 ) + ( period & 1 );
	}

	private static int periodToKey( int period )
	{
		int key = 0;
		if( period >= KEY_TO_PERIOD[ 72 ] && period <= KEY_TO_PERIOD[ 1 ] )
		{
			while( KEY_TO_PERIOD[ key + 12 ] > period )
			{
				key += 12;
			}
			while( KEY_TO_PERIOD[ key + 1 ] >= period )
			{
				key++;
			}
			if( ( KEY_TO_PERIOD[ key ] - period ) >= ( period - KEY_TO_PERIOD[ key + 1 ] ) )
			{
				key++;
			}
		}
		return key;
	}
	
	private static int transpose( int period, int semitones )
	{
		period = period * KEY_TO_PERIOD[ semitones + 13 ] * 2 / 856;
		return ( period >> 1 ) + ( period & 1 );
	}
	
	private static void clear( int[] array )
	{
		for( int idx = 0; idx < array.length; idx++ )
		{
			array[ idx ] = 0;
		}
	}
	
	private static void writeAscii( String text, byte[] outBuf, int offset, int len )
	{
		for( int idx = 0; idx < len; idx++ )
		{
			outBuf[ offset + idx ] = ( byte ) ( idx < text.length() ? text.charAt( idx ) : 32 );
		}
	}
	
	public static String readString( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = readBytes( inputStream, length );
		length = 0;
		for( int idx = 0; idx < bytes.length; idx++ )
		{
			if( ( bytes[ idx ] & 0xFF ) <= 32 )
			{
				bytes[ idx ] = 32;
			}
			else
			{
				length = idx + 1;
			}
		}
		return new String( bytes, 0, length, "ISO-8859-1" );
	}
	
	public static byte[] readBytes( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = new byte[ length ];
		int offset = 0;
		int count = 0;
		while( offset < length && count >= 0 )
		{
			offset += count;
			count = inputStream.read( bytes, offset, length - offset );
		}
		return bytes;
	}
	
	public static String pad( String string, char chr, int length, boolean left )
	{
		if( string.length() < length )
		{
			char[] chars = new char[ length ];
			for( int idx = 0; idx < chars.length; idx++ )
			{
				chars[ idx ] = chr;
			}
			string.getChars( 0, string.length(), chars, left ? length - string.length() : 0 );
			return new String( chars );
		}
		return string;
	}
	
	public static void downsample( int[] buf, int count, int[] coeffs )
	{
		for( int idx = 0; idx < count; idx++ )
		{
			int lamp = 0;
			int ramp = 0;
			for( int coef = 0; coef < coeffs.length; coef++ )
			{
				lamp += ( buf[ idx * 4 + coef * 2 ] * coeffs[ coef ] ) >> FIXED_POINT_SHIFT;
				ramp += ( buf[ idx * 4 + coef * 2 + 1 ] * coeffs[ coef ] ) >> FIXED_POINT_SHIFT;
			}
			buf[ idx * 2 ] = lamp;
			buf[ idx * 2 + 1 ] = ramp;
		}
	}
	
	public static int reverb( int[] buf, int[] reverbBuf, int reverbIdx, int count )
	{
		for( int idx = 0; idx < count; idx++ )
		{
			buf[ idx * 2 ] = ( buf[ idx * 2 ] * 3 + reverbBuf[ reverbIdx + 1 ] ) >> 2;
			buf[ idx * 2 + 1 ] = ( buf[ idx * 2 + 1 ] * 3 + reverbBuf[ reverbIdx ] ) >> 2;
			reverbBuf[ reverbIdx ] = buf[ idx * 2 ];
			reverbBuf[ reverbIdx + 1 ] = buf[ idx * 2 + 1 ];
			reverbIdx += 2;
			if( reverbIdx >= reverbBuf.length )
			{
				reverbIdx = 0;
			}
		}
		return reverbIdx;
	}
	
	public static void clip( int[] inputBuf, byte[] outputBuf, int count )
	{
		for( int idx = 0; idx < count; idx++ )
		{
			int ampl = inputBuf[ idx ];
			if( ampl > 32767 )
			{
				ampl = 32767;
			}
			if( ampl < -32768 )
			{
				ampl = -32768;
			}
			outputBuf[ idx * 2 ] = ( byte ) ampl;
			outputBuf[ idx * 2 + 1 ] = ( byte ) ( ampl >> 8 );
		}
	}
	
	public static void main( String[] args ) throws Exception {
		//for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow( 2, ( 8 - idx ) / 96.0 ) * FIXED_POINT_ONE ) );
		//for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow( 2, idx / -12.0 ) * FIXED_POINT_ONE ) );
		final int SAMPLING_RATE = 48000;
		ModPlay3 modPlay3 = null;
		java.io.InputStream inputStream = new java.io.FileInputStream( args[ 0 ] );
		try
		{
			modPlay3 = new ModPlay3( inputStream, false );
		}
		catch( IllegalArgumentException e )
		{
			System.out.println( e.getMessage() + " Assuming Ultimate Soundtracker format." );
		}
		finally
		{
			inputStream.close();
		}
		if( modPlay3 == null )
		{
			inputStream = new java.io.FileInputStream( args[ 0 ] );
			try
			{
				modPlay3 = new ModPlay3( inputStream, true );
			}
			finally
			{
				inputStream.close();
			}
		}
		System.out.println( "ModPlay3 (C)2020 Martin Cameron!" );
		System.out.println( "Playing: " + pad( modPlay3.songName, ' ', 20, false ) + " Len   Loop" );
		for( int idx = 1; idx < MAX_SAMPLES && modPlay3.instrumentNames[ idx ] != null; idx++ )
		{
			if( modPlay3.sampleData[ idx ].length > 0 || modPlay3.instrumentNames[ idx ].length() > 0 )
			{
				int loop = modPlay3.sampleLoopLength[ idx ] >> FIXED_POINT_SHIFT;
				int len = ( modPlay3.sampleLoopStart[ idx ] >> FIXED_POINT_SHIFT ) + loop;
				System.out.println( pad( String.valueOf( idx ), '0', 2, true ) + ' '
					+ pad( modPlay3.instrumentNames[ idx ], ' ', 23, false )
					+ pad( String.valueOf( len ), ' ', 7, true ) + pad( String.valueOf( loop ), ' ', 7, true ) );
			}
		}
		javax.sound.sampled.AudioFormat audioFormat = new javax.sound.sampled.AudioFormat( SAMPLING_RATE, 16, 2, true, false );
		javax.sound.sampled.SourceDataLine sourceDataLine = ( javax.sound.sampled.SourceDataLine )
			javax.sound.sampled.AudioSystem.getLine( new javax.sound.sampled.DataLine.Info(
				javax.sound.sampled.SourceDataLine.class, audioFormat ) );
		sourceDataLine.open( audioFormat );
		sourceDataLine.start();
		final int DOWNSAMPLE_BUF_SAMPLES = 2048;
		byte[] outBuf = new byte[ DOWNSAMPLE_BUF_SAMPLES * 2 ];
		int[] reverbBuf = new int[ ( SAMPLING_RATE / 20 ) * 2 ];
		int[] downsampleBuf = new int[ ( DOWNSAMPLE_BUF_SAMPLES + FILTER_COEFFS.length ) * 2 ];
		int[] mixBuf = new int[ SAMPLING_RATE * 2 / 5 ];
		int mixIdx = 0, mixLen = 0, reverbIdx = 0;
		while( true )
		{
			System.arraycopy( downsampleBuf, DOWNSAMPLE_BUF_SAMPLES * 2, downsampleBuf, 0, FILTER_COEFFS.length * 2 );
			int offset = FILTER_COEFFS.length;
			int length = offset + DOWNSAMPLE_BUF_SAMPLES;
			while( offset < length )
			{
				if( mixIdx >= mixLen )
				{
					mixLen = modPlay3.getAudio( SAMPLING_RATE * 2, mixBuf );
					mixIdx = 0;
				}
				int count = length - offset;
				if( count > mixLen - mixIdx )
				{
					count = mixLen - mixIdx;
				}
				System.arraycopy( mixBuf, mixIdx * 2, downsampleBuf, offset * 2, count * 2 );
				mixIdx += count;
				offset += count;
			}
			downsample( downsampleBuf, DOWNSAMPLE_BUF_SAMPLES / 2, FILTER_COEFFS );
			reverbIdx = reverb( downsampleBuf, reverbBuf, reverbIdx, DOWNSAMPLE_BUF_SAMPLES / 2 );
			clip( downsampleBuf, outBuf, DOWNSAMPLE_BUF_SAMPLES );
			sourceDataLine.write( outBuf, 0, DOWNSAMPLE_BUF_SAMPLES * 2 );
		}
	}
}
